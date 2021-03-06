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

import fish.focus.schema.exchange.common.v1.AcknowledgeType;
import fish.focus.schema.exchange.module.v1.*;
import fish.focus.schema.exchange.movement.v1.MovementBaseType;
import fish.focus.schema.exchange.movement.v1.MovementSourceType;
import fish.focus.schema.exchange.movement.v1.SetReportMovementType;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.plugin.v1.AcknowledgeResponse;
import fish.focus.schema.exchange.plugin.v1.ExchangePluginMethod;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;
import fish.focus.schema.exchange.service.v1.StatusType;
import fish.focus.schema.exchange.v1.*;
import fish.focus.uvms.activity.model.mapper.ActivityModuleRequestMapper;
import fish.focus.uvms.activity.model.schemas.MessageType;
import fish.focus.uvms.activity.model.schemas.SyncAsyncRequestType;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.commons.message.impl.JAXBUtils;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleResponseMapper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.exchange.service.entity.exchangelog.ExchangeLog;
import fish.focus.uvms.exchange.service.entity.serviceregistry.Service;
import fish.focus.uvms.exchange.service.mapper.MovementMapper;
import fish.focus.uvms.exchange.service.mapper.ServiceMapper;
import fish.focus.uvms.exchange.service.message.event.ErrorEvent;
import fish.focus.uvms.exchange.service.message.event.PluginErrorEvent;
import fish.focus.uvms.exchange.service.message.event.carrier.ExchangeErrorEvent;
import fish.focus.uvms.exchange.service.message.event.carrier.PluginErrorEventCarrier;
import fish.focus.uvms.exchange.service.message.producer.bean.*;
import fish.focus.uvms.exchange.service.model.IncomingMovement;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.UUID;

import static fish.focus.uvms.commons.message.impl.JAXBUtils.unMarshallMessage;


@Stateless
public class ExchangeEventIncomingServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeEventIncomingServiceBean.class);

    @Inject
    @ErrorEvent
    private Event<ExchangeErrorEvent> exchangeErrorEvent;

    @Inject
    @PluginErrorEvent
    private Event<PluginErrorEventCarrier> pluginErrorEvent;

    @Inject
    private ServiceRegistryModelBean serviceRegistryModel;

    @EJB
    private ExchangeLogServiceBean exchangeLogService;

    @EJB
    private ExchangeLogModelBean exchangeLogModel;


    @EJB
    private ExchangeEventOutgoingServiceBean exchangeEventOutgoingService;

    @Inject
    private ExchangeAssetProducer exchangeAssetProducer;

    @Inject
    private ExchangeMovementProducer movementProducer;

    @Inject
    private ExchangeActivityProducer exchangeActivityProducer;

    @Inject
    private ExchangeSalesProducer salesProducer;

    private Jsonb jsonb = new JsonBConfigurator().getContext(null);

    /**
     * Process FLUXFAReportMessage
     *
     * @param message
     */
    public void processFLUXFAReportMessage(TextMessage message) {
        try {
            SetFLUXFAReportMessageRequest request = JAXBMarshaller.unmarshallTextMessage(message, SetFLUXFAReportMessageRequest.class);
            LOG.debug("Got FLUXFAReportMessage in exchange : {}", request.getRequest());

            ExchangeLog exchangeLog = exchangeLogService.log(request, LogType.RCV_FLUX_FA_REPORT_MSG, ExchangeLogStatusTypeType.SUCCESSFUL, extractFaType(request.getMethod()), request.getRequest(), true);

            String activityRequest = ActivityModuleRequestMapper.mapToSetFLUXFAReportOrQueryMessageRequest(request.getRequest(), PluginType.valueOf(request.getPluginType().name()).name(), MessageType.FLUX_FA_REPORT_MESSAGE, SyncAsyncRequestType.ASYNC, exchangeLog.getId().toString());
            exchangeActivityProducer.sendActivityMessage(activityRequest);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not process FA message", e);
        }
    }

    public void processFAQueryMessage(TextMessage message) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    public void processFluxFAResponseMessage(TextMessage message) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    /**
     * Method for Observing the @MdrSyncMessageEvent, meaning a message from Activity MDR
     * module has arrived (synchronisation of the mdr).
     * Process MDR sync response message sent to Flux MDR plugin
     *
     * @param message
     */
    public void sendResponseToRulesModule(TextMessage message) { // And nothing to the exchange log?
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    /**
     * Get plugin list from APP module
     *
     * @param message
     */
    public void getPluginListByTypes(TextMessage message) {
        try {
            GetServiceListRequest request = JAXBMarshaller.unmarshallTextMessage(message, GetServiceListRequest.class);
            LOG.info("[INFO] Get plugin config LIST_SERVICE:{}", request.getType());

            List<ServiceResponseType> serviceList = ServiceMapper.toServiceModelList(serviceRegistryModel.getPlugins(request.getType()));
            exchangeAssetProducer.sendResponseMessageToSender(message, ExchangeModuleResponseMapper.mapServiceListResponse(serviceList));
        } catch (Exception e) {
            LOG.error("[ Error when getting plugin list from source {}] {}", message, e);
            exchangeErrorEvent.fire(new ExchangeErrorEvent(message, "Excpetion when getting service list"));
        }
    }

    public void processReceivedMovementBatch(TextMessage message) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    /**
     * Process a received Movement
     *
     * @param message
     */
    public void processMovement(TextMessage message) {
        try {
            SetMovementReportRequest request = JAXBMarshaller.unmarshallTextMessage(message, SetMovementReportRequest.class);
            if (request.getUsername() == null) {
                LOG.error("[ Error when receiving message in exchange, username must be set in the request: ]");
                exchangeErrorEvent.fire(new ExchangeErrorEvent(message, "Username in the request must be set"));
                return;
            }

            LOG.trace("Processing Movement : {}", request.getRefGuid());
            String username;
            SetReportMovementType setRepMovType = request.getRequest();
            if (MovementSourceType.MANUAL.equals(setRepMovType.getMovement().getSource())) {// A person has created a position
                username = request.getUsername();
            } else {// A plugin has reported a position
                username = setRepMovType.getPluginType().name();
            }

            String pluginName = setRepMovType.getPluginName();
            PluginType pluginType = setRepMovType.getPluginType();
            if (validateMovementReport(setRepMovType, pluginName, message)) {
                MovementBaseType baseMovement = setRepMovType.getMovement();
                IncomingMovement incomingMovement = MovementMapper.mapMovementBaseTypeToRawMovementType(baseMovement);
                incomingMovement.setPluginType(pluginType.value());
                incomingMovement.setDateReceived(setRepMovType.getTimestamp().toInstant());
                incomingMovement.setUpdatedBy(username);
                if (!baseMovement.getSource().equals(MovementSourceType.AIS)) {
                    LOG.debug("Logging received movement.");
                    ExchangeLog createdLog = exchangeLogService.log(request, LogType.RECEIVE_MOVEMENT, ExchangeLogStatusTypeType.ISSUED, TypeRefType.MOVEMENT,
                            setRepMovType.getOriginalIncomingMessage(), true);
                    if (setRepMovType.getPollRef() != null) {
                        createdLog.setRelatedRefGuid(UUID.fromString(setRepMovType.getPollRef()));
                        createdLog.setRelatedRefType(TypeRefType.POLL);
                    }
                    incomingMovement.setAckResponseMessageId(createdLog.getId().toString());
                }

                String json = jsonb.toJson(incomingMovement);
                // Combine all possible values into one big grouping string
                String groupId = incomingMovement.getAssetCFR() + incomingMovement.getAssetIMO() + incomingMovement.getAssetIRCS() +
                        incomingMovement.getAssetMMSI() + incomingMovement.getAssetID() + incomingMovement.getAssetGuid() +
                        incomingMovement.getMobileTerminalDNID() + incomingMovement.getMobileTerminalConnectId() +
                        incomingMovement.getMobileTerminalGuid() + incomingMovement.getMobileTerminalLES() +
                        incomingMovement.getMobileTerminalMemberNumber() + incomingMovement.getMobileTerminalSerialNumber() + "AllOtherThings";
                movementProducer.sendMovementMessage(json, groupId);
                LOG.debug("Finished forwarding received movement to movement module.");
            } else {
                LOG.debug("Validation error. Event sent to plugin {}", message);    //This sending happens in validateMovementReport
            }
        } catch (Exception e) {
            LOG.error("Could not process SetMovementReportRequest", e);
            throw new RuntimeException("Could not process SetMovementReportRequest", e);
        }
    }

    public void processEfrSaveActivity(TextMessage textMessage) {
        try {
            LOG.debug("Received EFR Save Activity-message");
            exchangeActivityProducer.sendEfrSaveActivity(textMessage.getText());
        } catch (JMSException e) {
            final String ERROR_MESSAGE = "Could not process EfrSaveActivity";
            LOG.error(ERROR_MESSAGE, e);
            throw new IllegalArgumentException(ERROR_MESSAGE, e);
        }
    }

    /**
     * Logs and sends a received asset information to Asset
     *
     * @param event received asset information message
     */
    public void receiveAssetInformation(TextMessage event) {
        try {
            ReceiveAssetInformationRequest request = JAXBMarshaller.unmarshallTextMessage(event, ReceiveAssetInformationRequest.class);
            String message = request.getAssets();
            forwardToAsset(message);
            exchangeLogService.log(request, LogType.RECEIVE_ASSET_INFORMATION, ExchangeLogStatusTypeType.SUCCESSFUL, TypeRefType.ASSETS, message, true);
        } catch (Exception e) {
            try {
                String errorMessage = "Couldn't map to ReceiveAssetInformationRequest when processing asset information from plugin. The event was " + event.getText();
                firePluginFault(event, errorMessage, e, null);
            } catch (JMSException e1) {
                firePluginFault(event, "Couldn't map to ReceiveAssetInformationRequest when processing asset information from plugin.", e, null);
            }
        }
    }

    /**
     * Logs and sends a received sales report through to Rules
     *
     * @param event received sales report
     */
    public void receiveSalesReport(TextMessage event) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    /**
     * Logs and sends a received sales query through to Rules
     *
     * @param event received sales query
     */
    public void receiveSalesQuery(TextMessage event) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }

    /**
     * Logs and sends a received sales response through to Rules
     *
     * @param event
     */
    public void receiveSalesResponse(TextMessage event) {
        throw new NotImplementedException("Rules has been removed since it is not in use and is not being maintained");
    }


    public void receiveInvalidSalesMessage(TextMessage event) {
        try {
            ReceiveInvalidSalesMessage request = JAXBMarshaller.unmarshallTextMessage(event, ReceiveInvalidSalesMessage.class);
            exchangeLogService.log(request, LogType.RECEIVE_SALES_REPORT, ExchangeLogStatusTypeType.FAILED, TypeRefType.SALES_REPORT, request.getOriginalMessage(), true);
            salesProducer.sendSalesMessage(request.getRespondToInvalidMessageRequest());
        } catch (Exception e) {
            firePluginFault(event, "Could not log the incoming invalid sales message", e, null);
        }
    }

    /**
     * Checks for a reference in log table for a certain type of message
     *
     * @param event
     */
    public void logRefIdByTypeExists(TextMessage event) { // This one has the weird behaviour that it both returns the correct answer AND puts the initial message in DLQ for causing an exception AT THE SAME TIME if the input is an empty list.
        try {
            LogRefIdByTypeExistsRequest request = unMarshallMessage(event.getText(), LogRefIdByTypeExistsRequest.class);
            UUID refGuid = UUID.fromString(request.getRefGuid());
            List<TypeRefType> refTypes = request.getRefTypes();
            List<ExchangeLogStatusType> exchangeStatusHistoryList = exchangeLogModel.getExchangeLogsStatusHistories(refGuid, refTypes);

            LogRefIdByTypeExistsResponse response = new LogRefIdByTypeExistsResponse();
            if (exchangeStatusHistoryList != null && !exchangeStatusHistoryList.isEmpty()) {
                response.setRefGuid(exchangeStatusHistoryList.get(0).getTypeRef().getRefGuid());
            }

            String responseAsString = JAXBUtils.marshallJaxBObjectToString(response);
            exchangeAssetProducer.sendResponseMessageToSender(event, responseAsString);
        } catch (JAXBException | JMSException e) {
            fireExchangeFault(event, "Could not un-marshall " + LogRefIdByTypeExistsRequest.class, e);
        }
    }

    /**
     * Checks for a guid in log table for a certain type of message
     *
     * @param event
     */
    public void logIdByTypeExists(TextMessage event) {
        try {
            LogIdByTypeExistsRequest request = unMarshallMessage(event.getText(), LogIdByTypeExistsRequest.class);
            UUID messageGuid = UUID.fromString(request.getMessageGuid());
            TypeRefType refType = request.getRefType();
            ExchangeLogType exchangeLogByGuid = exchangeLogModel.getExchangeLogByGuidAndType(messageGuid, refType);

            LogIdByTypeExistsResponse response = new LogIdByTypeExistsResponse();
            if (exchangeLogByGuid != null) {
                response.setMessageGuid(exchangeLogByGuid.getGuid());
            }

            String responseAsString = JAXBUtils.marshallJaxBObjectToString(response);
            exchangeAssetProducer.sendResponseMessageToSender(event, responseAsString);

        } catch (JAXBException | JMSException e) {
            fireExchangeFault(event, "Could not un-marshall " + LogRefIdByTypeExistsRequest.class, e);
        }

    }

    /**
     * Logs and sends a query asset information to FLUX fleet plugin
     *
     * @param event query asset information message
     */
    public void queryAssetInformation(TextMessage event) {
        try {
            QueryAssetInformationRequest incomingRequest = JAXBMarshaller.unmarshallTextMessage(event, QueryAssetInformationRequest.class);
            String message = incomingRequest.getAssets();
            String destination = incomingRequest.getDestination();
            String senderOrReceiver = incomingRequest.getSenderOrReceiver();

            fish.focus.schema.exchange.plugin.v1.SendQueryAssetInformationRequest outgoingRequest = new fish.focus.schema.exchange.plugin.v1.SendQueryAssetInformationRequest();
            outgoingRequest.setQuery(message);
            outgoingRequest.setDestination(destination);
            outgoingRequest.setSenderOrReceiver(senderOrReceiver);
            outgoingRequest.setMethod(ExchangePluginMethod.SEND_VESSEL_QUERY);

            exchangeEventOutgoingService.sendAssetInformationToFLUX(outgoingRequest);
            exchangeLogService.log(incomingRequest, LogType.QUERY_ASSET_INFORMATION, ExchangeLogStatusTypeType.SUCCESSFUL, TypeRefType.ASSETS, message, false);
        } catch (Exception e) {
            fireExchangeFault(event, "Error when sending asset information query to FLUX", e);
            firePluginFault(event, "Could not log the outgoing asset information query.", e, null);
        }
    }

    /**
     * Ping Exchange APP module
     *
     * @param message
     */
    public void ping(TextMessage message) {
        try {
            PingResponse response = new PingResponse();
            response.setResponse("pong");
            exchangeAssetProducer.sendResponseMessageToSender(message, JAXBMarshaller.marshallJaxBObjectToString(response));
        } catch (Exception e) {
            LOG.error("[ Error when marshalling ping response ]");
        }
    }

    /**
     * Process answer of ping sent to plugins
     *
     * @param message
     */
    public void processPluginPing(TextMessage message) {
        try {
            fish.focus.schema.exchange.plugin.v1.PingResponse response = JAXBMarshaller.unmarshallTextMessage(message, fish.focus.schema.exchange.plugin.v1.PingResponse.class);
            //TODO handle ping response from plugin, eg. no serviceClassName in response
            LOG.info("FIX ME handle ping response from plugin");
        } catch (Exception e) {
            LOG.error("Couldn't process ping response from plugin {} {} ", message, e.getMessage());
        }
    }

    /**
     * Process answer of commands sent to plugins
     *
     * @param message
     */
    public void processAcknowledge(TextMessage message) {
        try {
            AcknowledgeResponse response = JAXBMarshaller.unmarshallTextMessage(message, AcknowledgeResponse.class);
            AcknowledgeType acknowledge = response.getResponse();
            String serviceClassName = response.getServiceClassName();
            ExchangePluginMethod method = response.getMethod();
            LOG.info("[INFO] Process acknowledge : {}", method);
            switch (method) {
                case SET_COMMAND:
                    // Only Acknowledge for poll should have a poll status set
                    if (acknowledge.getPollStatus() != null && acknowledge.getPollStatus().getPollId() != null) {
                        handleSetPollStatusAcknowledge(method, serviceClassName, acknowledge);
                    } else {
                        handleUpdateExchangeLogAcknowledge(method, serviceClassName, acknowledge);
                    }
                    break;
                case SET_REPORT:
                    handleUpdateExchangeLogAcknowledge(method, serviceClassName, acknowledge);
                    break;
                case START:
                    handleUpdateServiceAcknowledge(serviceClassName, acknowledge, StatusType.STARTED);
                    break;
                case STOP:
                    handleUpdateServiceAcknowledge(serviceClassName, acknowledge, StatusType.STOPPED);
                    break;
                case SET_CONFIG:
                default:
                    handleAcknowledge(method, serviceClassName, acknowledge);
                    break;
            }
        } catch (Exception e) {
            LOG.error("Process acknowledge couldn't be marshalled {} {}", message, e);
            throw new IllegalStateException("Could not process acknowledge", e);
        }
    }

    /**
     * forwards serialized message to Asset module
     *
     * @param messageToForward
     */
    private void forwardToAsset(String messageToForward) {
        try {
            LOG.info("Forwarding the message to Asset.");
            String s = exchangeAssetProducer.forwardToAsset(messageToForward, "ASSET_INFORMATION");
        } catch (Exception e) {
            LOG.error("Failed to forward message to Asset: {} {}", messageToForward, e);
        }
    }


    private void firePluginFault(TextMessage messageEvent, String errorMessage, Throwable exception, String serviceClassName) {
        try {
            LOG.error(errorMessage, exception);
            Service service = ((serviceClassName == null) ? null : serviceRegistryModel.getPlugin(serviceClassName));
            pluginErrorEvent.fire(new PluginErrorEventCarrier(messageEvent, service.getServiceResponse(), errorMessage));
        } catch (Exception e) {
            LOG.error("Unable to send PluginError message due to: {}", e.getMessage(), e);
        }
    }

    private void fireExchangeFault(TextMessage messageEvent, String errorMessage, Throwable exception) {
        LOG.error(errorMessage, exception);
        exchangeErrorEvent.fire(new ExchangeErrorEvent(messageEvent, errorMessage));
    }


    private boolean validateMovementReport(SetReportMovementType setReport, String service, TextMessage origin) {
        if (setReport == null) {
            String faultMessage = "No setReport request";
            firePluginFault(origin, faultMessage, new RuntimeException(), service);
            return false;
        } else if (setReport.getMovement() == null) {
            String faultMessage = "No movement in setReport request";
            firePluginFault(origin, faultMessage, new RuntimeException(), service);
            return false;
        } else if (setReport.getPluginType() == null) {
            String faultMessage = "No pluginType in setReport request";
            firePluginFault(origin, faultMessage, new RuntimeException(), service);
            return false;
        } else if (setReport.getPluginName() == null || setReport.getPluginName().isEmpty()) {
            String faultMessage = "No pluginName in setReport request";
            firePluginFault(origin, faultMessage, new RuntimeException(), service);
            return false;
        } else if (setReport.getTimestamp() == null) {
            String faultMessage = "No timestamp in setReport request";
            firePluginFault(origin, faultMessage, new RuntimeException(), service);
            return false;
        }
        return true;
    }

    private void handleUpdateExchangeLogAcknowledge(ExchangePluginMethod method, String serviceClassName, AcknowledgeType ack) {

        ExchangeLogStatusTypeType logStatus = ExchangeLogStatusTypeType.FAILED;
        if (ack.getType() == fish.focus.schema.exchange.common.v1.AcknowledgeTypeType.OK) {//TODO if(poll probably transmitted)
            logStatus = ExchangeLogStatusTypeType.SUCCESSFUL;
            exchangeLogService.removeUnsentMessage(ack.getUnsentMessageGuid());
        } else if (ack.getType() == fish.focus.schema.exchange.common.v1.AcknowledgeTypeType.NOK) {
            LOG.debug("{} was NOK: {}", method, ack.getMessage());
            exchangeLogService.acknowledgeUnsentMessage(ack.getUnsentMessageGuid());
        }

        if (ack.getMessage() != null) {
            exchangeLogService.updateLogMessage(ack.getLogId(), ack.getMessage());
        }

        exchangeLogService.updateStatus(ack.getLogId(), logStatus, serviceClassName);
    }

    private void handleSetPollStatusAcknowledge(ExchangePluginMethod method, String serviceClassName, AcknowledgeType ack) {
        LOG.debug("{} was acknowledged in {}", method, serviceClassName);
        ExchangeLogStatusTypeType exchangeLogStatus = ack.getPollStatus().getStatus();
        exchangeLogService.removeUnsentMessage(ack.getUnsentMessageGuid());
        exchangeLogService.setPollStatus(UUID.fromString(ack.getPollStatus().getPollId()), exchangeLogStatus, serviceClassName, ack.getMessage());
    }

    private void handleUpdateServiceAcknowledge(String serviceClassName, AcknowledgeType ack, StatusType status) {
        if (ack.getType() == fish.focus.schema.exchange.common.v1.AcknowledgeTypeType.OK) {
            serviceRegistryModel.updatePluginStatus(serviceClassName, status, serviceClassName);

        } else if (ack.getType() == fish.focus.schema.exchange.common.v1.AcknowledgeTypeType.NOK) {
            LOG.error("Couldn't start service {}", serviceClassName);

        }
    }

    private void handleAcknowledge(ExchangePluginMethod method, String serviceClassName, AcknowledgeType ack) {
        LOG.debug("{} was acknowledged in {}", method, serviceClassName);
        if (ack.getType() == fish.focus.schema.exchange.common.v1.AcknowledgeTypeType.NOK) {
            LOG.error(serviceClassName + " didn't like it. " + ack.getMessage());
        }
    }

    private TypeRefType extractFaType(ExchangeModuleMethod method) {
        TypeRefType faType = null;
        switch (method) {
            case SET_FLUX_FA_REPORT_MESSAGE:
                faType = TypeRefType.FA_REPORT;
                break;
            case UNKNOWN:
                faType = TypeRefType.UNKNOWN;
                break;
            default:
                LOG.error("[FATAL] FA Type could not be determined!!");
        }
        return faType;
    }

    private String extractLogId(TextMessage message, ExchangeLog exchangeLog) {
        String logId = null;
        if (exchangeLog == null) {
            LOG.error("ExchangeLogType received is NULL while trying to save {}", message);
        } else {
            logId = exchangeLog.getId().toString();
            LOG.info("Logged to Exchange message with following GUID :" + logId);
        }
        return logId;
    }

}
