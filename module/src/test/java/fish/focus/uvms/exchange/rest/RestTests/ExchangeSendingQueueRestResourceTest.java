package fish.focus.uvms.exchange.rest.RestTests;

import fish.focus.schema.exchange.common.v1.CommandTypeType;
import fish.focus.schema.exchange.plugin.types.v1.EmailType;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.service.v1.CapabilityTypeType;
import fish.focus.uvms.exchange.rest.BuildExchangeRestTestDeployment;
import fish.focus.uvms.exchange.rest.JMSHelper;
import fish.focus.uvms.exchange.rest.RestHelper;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.exchange.rest.dto.exchange.SendingGroupLog;
import fish.focus.uvms.exchange.service.bean.ExchangeLogServiceBean;
import fish.focus.uvms.exchange.service.dao.ServiceRegistryDaoBean;
import fish.focus.uvms.exchange.service.dao.UnsentMessageDaoBean;
import fish.focus.uvms.exchange.service.entity.serviceregistry.Service;
import fish.focus.uvms.exchange.service.entity.serviceregistry.ServiceCapability;
import fish.focus.uvms.exchange.service.entity.unsent.UnsentMessage;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ExchangeSendingQueueRestResourceTest extends BuildExchangeRestTestDeployment {

    @Inject
    private ExchangeLogServiceBean exchangeLogService;

    @Inject
    private UnsentMessageDaoBean unsentMessageDao;

    @Inject
    private ServiceRegistryDaoBean serviceRegistryDao;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Test
    @OperateOnDeployment("exchangeservice")
    public void getSendingQueueTest() throws Exception {
        String unsentMessageId = exchangeLogService.createUnsentMessage(
                "Sending queue test senderReceiver", Instant.now(),
                "Sending queue test recipient", "Sending queue test message",
                new ArrayList<>(), "Sending queue test username", null);

        String stringResponse = getWebTarget()
                .path("sendingqueue")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        assertNotNull(stringResponse);
        assertTrue(stringResponse.contains(unsentMessageId));
        List<SendingGroupLog> response = RestHelper.readResponseDtoList(stringResponse, SendingGroupLog.class);
        assertFalse(response.isEmpty());
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void sendTest() throws Exception {
        JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        String serviceName = "Email Test Service";
        String serviceClassName = "eu.europa.ec.fisheries.uvms.plugins.sweagencyemail";
        int sizeB4 = unsentMessageDao.getAll().size();
        Service service = createAndPersistBasicService(serviceName, serviceClassName, PluginType.EMAIL);

        EmailType email = new EmailType();
        email.setBody("Test body");
        email.setFrom("ExchangeTests@exchange.uvms");
        email.setTo("TestExecuter@exchange.uvms");
        email.setSubject("Test subject");

        String request = ExchangeModuleRequestMapper.createMarshalledSetCommandSendEmailRequest(serviceClassName, email, null);

        jmsHelper.registerSubscriber("ServiceName = '" + serviceClassName + "'");
        String corrID = jmsHelper.sendExchangeMessage(request, null, "SET_COMMAND");
        TextMessage message = (TextMessage) jmsHelper.listenOnEventBus("ServiceName = '" + serviceClassName + "'", 5000L);
        SetCommandRequest response = JAXBMarshaller.unmarshallTextMessage(message, SetCommandRequest.class);

        assertNotNull(response);
        assertEquals(response.getCommand().getCommand(), CommandTypeType.EMAIL);
        assertEquals(response.getCommand().getPluginName(), serviceClassName);

        Thread.sleep(1000);     //to allow the db to sync up
        List<UnsentMessage> unsentMessageList = unsentMessageDao.getAll();
        assertEquals(sizeB4 + 1, unsentMessageList.size());

        List<String> unsentMessagesIdList = new ArrayList<>();
        for (UnsentMessage u : unsentMessageList) {
            unsentMessagesIdList.add(u.getGuid().toString());
        }

        jmsHelper.registerSubscriber("ServiceName = '" + serviceClassName + "'");
        String stringResponse = getWebTarget()
                .path("sendingqueue")
                .path("send")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(unsentMessagesIdList), String.class);

        TextMessage resentMessage = (TextMessage) jmsHelper.listenOnEventBus("ServiceName = '" + serviceClassName + "'", 5000l);
        SetCommandRequest resentResponse = JAXBMarshaller.unmarshallTextMessage(resentMessage, SetCommandRequest.class);

        assertNotNull(resentResponse);
        assertEquals(resentResponse.getCommand().getCommand(), CommandTypeType.EMAIL);
        assertEquals(resentResponse.getCommand().getPluginName(), serviceClassName);

        serviceRegistryDao.deleteEntity(service.getId());
    }

    private Service createAndPersistBasicService(String name, String serviceClassName, PluginType pluginType) throws Exception {
        Service s = serviceRegistryDao.getServiceByServiceClassName(serviceClassName);
        if (s != null) {
            serviceRegistryDao.deleteEntity(s.getId());
            Thread.sleep(1000);
        }

        s = new Service();
        s.setActive(true);
        s.setDescription("Test description");
        s.setName(name);
        s.setSatelliteType(null);
        s.setServiceClassName(serviceClassName);
        s.setServiceResponse(serviceClassName + "PLUGIN_RESPONSE");
        s.setStatus(true);
        s.setType(pluginType);
        s.setUpdated(Instant.now());
        s.setUpdatedBy("Exchange Tests");

        List<ServiceCapability> serviceCapabilityList = new ArrayList<>();
        ServiceCapability serviceCapability = new ServiceCapability();
        serviceCapability.setService(s);
        serviceCapability.setUpdatedBy("Exchange Tests");
        serviceCapability.setUpdatedTime(Instant.now());
        serviceCapability.setCapability(CapabilityTypeType.POLLABLE);
        serviceCapability.setValue(true);
        serviceCapabilityList.add(serviceCapability);
        s.setServiceCapabilityList(serviceCapabilityList);

        s.setServiceSettingList(new ArrayList<>());

        return serviceRegistryDao.createEntity(s);
    }
}
