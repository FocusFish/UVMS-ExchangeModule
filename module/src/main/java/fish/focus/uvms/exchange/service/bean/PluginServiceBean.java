/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.exchange.service.bean;

import fish.focus.schema.exchange.module.v1.UpdatePluginSettingRequest;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.registry.v1.RegisterServiceRequest;
import fish.focus.schema.exchange.registry.v1.UnregisterServiceRequest;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;
import fish.focus.uvms.config.event.ConfigSettingEvent;
import fish.focus.uvms.config.event.ConfigSettingUpdatedEvent;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.config.service.UVMSConfigService;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleResponseMapper;
import fish.focus.uvms.exchange.model.mapper.ExchangePluginRequestMapper;
import fish.focus.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.exchange.service.dao.ServiceRegistryDaoBean;
import fish.focus.uvms.exchange.service.entity.serviceregistry.Service;
import fish.focus.uvms.exchange.service.entity.serviceregistry.ServiceSetting;
import fish.focus.uvms.exchange.service.mapper.ServiceMapper;
import fish.focus.uvms.exchange.service.mapper.SettingTypeMapper;
import fish.focus.uvms.exchange.service.message.event.ErrorEvent;
import fish.focus.uvms.exchange.service.message.event.PluginErrorEvent;
import fish.focus.uvms.exchange.service.message.event.carrier.ExchangeErrorEvent;
import fish.focus.uvms.exchange.service.message.event.carrier.PluginErrorEventCarrier;
import fish.focus.uvms.exchange.service.message.producer.bean.EventProducer;
import fish.focus.uvms.exchange.service.message.producer.bean.ExchangeEventBusTopicProducer;
import fish.focus.uvms.exchange.service.message.producer.bean.ExchangeEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class PluginServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(PluginServiceBean.class);

    private static final String PARAMETER_DELIMETER = "\\.";

    @Inject
    @PluginErrorEvent
    private Event<PluginErrorEventCarrier> pluginErrorEvent;

    @Inject
    @ErrorEvent
    private Event<ExchangeErrorEvent> exchangeErrorEvent;

    @Inject
    ServiceRegistryDaoBean serviceRegistryDao;

    @Inject
    private ServiceRegistryModelBean serviceRegistryModel;

    @Inject
    private ExchangeEventBusTopicProducer eventBusTopicProducer;

    @Inject
    private ExchangeEventProducer exchangeEventProducer;

    @EJB
    private ParameterService parameterService;

    @EJB
    private UVMSConfigService configService;

    @Inject
    private EventProducer eventProducer;

    private boolean checkPluginType(PluginType pluginType, String responseTopicMessageSelector, String serviceClassName, String messageId) {
        LOG.debug("[INFO] CheckPluginType " + pluginType.name());
        if (PluginType.EMAIL == pluginType || PluginType.NAF == pluginType) {
            //Check if type already exists
            List<PluginType> type = new ArrayList<>();
            type.add(pluginType);
            try {
                List<Service> services = serviceRegistryModel.getPlugins(type);
                if (!services.isEmpty()) {
                    for(Service service : services){
                        if(service.getActive()){
                            if(service.getServiceClassName().equals(serviceClassName)){ // If we are trying to register something that is already registered.
                                return true;
                            }
                            //TODO log to audit log
                            String response = ExchangePluginResponseMapper.mapToRegisterServiceResponseNOK(messageId, "Plugin of " + pluginType + " already registered. Only one is allowed.");
                            eventBusTopicProducer.sendEventBusMessage(response, responseTopicMessageSelector);
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                String response = ExchangePluginResponseMapper.mapToRegisterServiceResponseNOK(messageId, "Exchange service exception when registering plugin [ " + e.getMessage() + " ]");
                eventBusTopicProducer.sendEventBusMessage(response, responseTopicMessageSelector);
                return false;
            }
        }
        return true;
    }

    private void registerService(RegisterServiceRequest register, Service newService, String messageId) {
        try {
            overrideSettingsFromConfig(newService); // Aka if config has settings for xyz parameter, use configs version instead
            Service service = serviceRegistryModel.registerService(newService, register.getService().getName());
            String serviceClassName = service.getServiceClassName();
            for (ServiceSetting setting : service.getServiceSettingList()) {
                String description = "Plugin " + serviceClassName + " " + setting.getSetting() + " setting";
                configService.pushSettingToConfig(SettingTypeMapper.map(setting.getSetting(), setting.getValue(),
                        description), false);
            }
            //TODO log to exchange log

            ServiceResponseType serviceModel = ServiceMapper.toServiceModel(service);
            String response = ExchangePluginResponseMapper.mapToRegisterServiceResponseOK(messageId, serviceModel);
            eventBusTopicProducer.sendEventBusMessage(response, register.getService().getServiceResponseMessageName());
            setServiceStatusOnRegister(service);

            eventProducer.sendServiceRegisteredEvent(serviceModel);
        } catch (Exception e) {
            String response = ExchangePluginResponseMapper.mapToRegisterServiceResponseNOK(messageId, "Exchange service exception when registering plugin [ " + e.getMessage() + " ]");
            eventBusTopicProducer.sendEventBusMessage(response, register.getService().getServiceResponseMessageName());
        }
    }

    private void setServiceStatusOnRegister(Service service) {
        if (service != null) {
            boolean status = service.getStatus();
            if (status) {     //StatusType.STARTED.equals
                LOG.info("Starting service {}", service);
                start(service.getServiceClassName());
            } else {
                LOG.info("Stopping service {}", service);
                stop(service.getServiceClassName());
            }
        }
    }

    public void registerService(TextMessage message) {
        RegisterServiceRequest register = null;
        Service newService = null;
        try {
            register = JAXBMarshaller.unmarshallTextMessage(message, RegisterServiceRequest.class);
            newService = ServiceMapper.toServiceEntity(register.getService(), register.getCapabilityList(), register.getSettingList(), register.getService().getName());
            LOG.info("[INFO] Received @RegisterServiceEvent : {}" , register.getService());
            String messageId = message.getJMSMessageID();
            boolean sendMessage;
            if (register.getService() != null) {
                sendMessage = checkPluginType(register.getService().getPluginType(), register.getService().getServiceResponseMessageName(), register.getService().getServiceClassName(), messageId);
                if (sendMessage) { // Aka if we should actually register. If it is an email or naf and we already have one of those active (that is not the same as the one we are trying to register) then no
                    registerService(register, newService,  messageId);
                }
            }
        } catch (Exception e) {
            LOG.error("[ERROR] Register service exception {} {}", message, e.getMessage());
            pluginErrorEvent.fire(new PluginErrorEventCarrier(message, newService.getServiceResponse(), "Exception when register service"));
        }
    }

    private void overrideSettingsFromConfig(Service service) {
        List<ServiceSetting> currentRequestSettings = service.getServiceSettingList();
        try {
            List<fish.focus.schema.config.types.v1.SettingType> configServiceSettings = configService.getSettings(service.getServiceClassName());
            Map<String, ServiceSetting>configServiceSettingsMap = putConfigSettingsInAMap(configServiceSettings);
            if(!configServiceSettingsMap.isEmpty()){
                for(ServiceSetting type : currentRequestSettings){
                    ServiceSetting configSetting = configServiceSettingsMap.get(type.getSetting());
                    if(configSetting!=null && !configSetting.getValue().equalsIgnoreCase(type.getValue())){
                        type.setValue(configSetting.getValue());
                    }
                }
            }

        } catch (ConfigServiceException e) {
            LOG.error("Register service exception, cannot read Exchange settings from Config {} {}", service, e.getMessage());
            // Ignore when we can't get the settings from Config. It is possible there is no Config module setup.
        }
    }

    private Map<String, ServiceSetting> putConfigSettingsInAMap(List<fish.focus.schema.config.types.v1.SettingType> settings){
        Map<String, ServiceSetting> settingMap = new HashMap<>();
        if(settings!=null && !settings.isEmpty()) {
            for (fish.focus.schema.config.types.v1.SettingType configSettingType : settings) {

                ServiceSetting ss = new ServiceSetting();
                ss.setUser("CONFIG");
                ss.setSetting(configSettingType.getKey());
                ss.setValue(configSettingType.getValue());
                settingMap.put(configSettingType.getKey(), ss);
            }
        }
        return  settingMap;
    }

    public void unregisterService(TextMessage message) {
        LOG.trace("[INFO] Received @UnRegisterServiceEvent request : {}", message);
        Service service = null;
        try {
            UnregisterServiceRequest unregister = JAXBMarshaller.unmarshallTextMessage(message, UnregisterServiceRequest.class);
            service = serviceRegistryModel.unregisterService(unregister.getService().getServiceClassName(), unregister.getService().getName());
            //NO ack back to plugin
            //TODO log to exchange log
            eventProducer.sendServiceUnregisteredEvent(ServiceMapper.toServiceModel(service));
        } catch (Exception e) {
            LOG.error("Unregister service exception " + e.getMessage());
            pluginErrorEvent.fire(new PluginErrorEventCarrier(message, service.getServiceResponse(), "Exception when unregister service"));
        }
    }
    
    private void updatePluginSetting(String serviceClassName, ServiceSetting updatedSetting, String username) {
        Service service = serviceRegistryModel.updatePluginSettings(serviceClassName, updatedSetting, username);
        // Send the plugin settings to the topic where all plugins should listen to
    	String text = ExchangePluginRequestMapper.createSetConfigRequest(ServiceMapper.toSettingListModel(service.getServiceSettingList()));
        eventBusTopicProducer.sendEventBusMessage(text, serviceClassName);
    }
    
    public void setConfig(@Observes @ConfigSettingUpdatedEvent ConfigSettingEvent settingEvent) {
        switch (settingEvent.getType()) {
            case STORE:
                //ConfigModule and/or Exchange module deployed
                break;
            case UPDATE:
                LOG.info("ConfigModule updated parameter table with settings of plugins");
                try {
                    String key = settingEvent.getKey();
                    String value = parameterService.getStringValue(key);
                    String[] splittedKey = key.split(PARAMETER_DELIMETER);

                    if (splittedKey.length > 2) {
                        String serviceClassName = "";
                        for (int i = 0; i < splittedKey.length - 2; i++) {
                            serviceClassName += splittedKey[i] + ".";
                        }
                        serviceClassName += splittedKey[splittedKey.length - 2];

                        ServiceSetting setting = new ServiceSetting();
                        setting.setSetting(key);
                        setting.setValue(value);
                        updatePluginSetting(serviceClassName, setting, "UVMS");
                    } else {
                        LOG.error("No key or malformed key sent in settingEvent: key: {}, value: {}", key, value);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                break;
            case DELETE:
                LOG.info("ConfigModule removed parameter setting");
                break;
        }
    }

	public void updatePluginSetting(TextMessage settingEvent) {
		try {
            UpdatePluginSettingRequest request = JAXBMarshaller.unmarshallTextMessage(settingEvent, UpdatePluginSettingRequest.class);
            if(request.getUsername() == null){
                LOG.error("[ Error when receiving message in exchange, username must be set in the request: ]");
                exchangeErrorEvent.fire(new ExchangeErrorEvent(settingEvent, "Username in the request must be set"));
                return;
            }
            LOG.info("Received @UpdatePluginSettingEvent from module queue:{}" , request.toString());
            ServiceSetting setting = ServiceMapper.simpleToSettingEntity(request.getSetting());
			updatePluginSetting(request.getServiceClassName(), setting, request.getUsername());
			String text = ExchangeModuleResponseMapper.mapUpdateSettingResponse(ExchangeModuleResponseMapper.mapAcknowledgeTypeOK());
            exchangeEventProducer.sendResponseMessageToSender(settingEvent, text);
		} catch (Exception e) {
			LOG.error("Couldn't unmarshall update setting request");
            ExchangeErrorEvent event = new ExchangeErrorEvent(settingEvent, "Couldn't update plugin setting");
			exchangeErrorEvent.fire(event);
		}
	}
    
    public boolean start(String serviceClassName) {
        if (serviceClassName == null) {
            throw new IllegalArgumentException("No service to start");
        }
        if (isServiceRegistered(serviceClassName)){
            String text = ExchangePluginRequestMapper.createStartRequest();
            eventBusTopicProducer.sendEventBusMessage(text, serviceClassName);
            return true;
        }else{
            throw new IllegalArgumentException("Service with service class name: "+ serviceClassName + " does not exist");
        }
    }

    private boolean isServiceRegistered(String serviceClassName) {
        Service checkRegistered = serviceRegistryDao.getServiceByServiceClassName(serviceClassName);
        return checkRegistered != null;
    }

    public boolean stop(String serviceClassName) {
        if (serviceClassName == null) {
            throw new IllegalArgumentException("No service to stop");
        }
        if(isServiceRegistered(serviceClassName)) {
            String text = ExchangePluginRequestMapper.createStopRequest();
            eventBusTopicProducer.sendEventBusMessage(text, serviceClassName);
            return true;
        }else{
            throw new IllegalArgumentException("Service with service class name: "+ serviceClassName + " does not exist");
        }
    }

    public boolean ping(String serviceClassName) {
        if (serviceClassName == null) {
            throw new IllegalArgumentException("No service to ping");
        }
        String text = ExchangePluginRequestMapper.createPingRequest();
        eventBusTopicProducer.sendEventBusMessage(text, serviceClassName);
        return true;

    }
}
