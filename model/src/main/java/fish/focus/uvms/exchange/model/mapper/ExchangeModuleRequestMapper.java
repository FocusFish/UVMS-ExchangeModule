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
package fish.focus.uvms.exchange.model.mapper;

import static fish.focus.schema.exchange.module.v1.ExchangeModuleMethod.QUERY_ASSET_INFORMATION;
import static fish.focus.schema.exchange.module.v1.ExchangeModuleMethod.RECEIVE_ASSET_INFORMATION;
import static fish.focus.schema.exchange.module.v1.ExchangeModuleMethod.SEND_ASSET_INFORMATION;

import javax.xml.bind.JAXBException;
import java.time.Instant;
import java.util.Date;  //leave be
import java.util.List;

import fish.focus.schema.exchange.common.v1.CommandType;
import fish.focus.schema.exchange.common.v1.CommandTypeType;
import fish.focus.schema.exchange.module.v1.*;
import fish.focus.schema.exchange.movement.v1.MovementRefType;
import fish.focus.schema.exchange.movement.v1.MovementType;
import fish.focus.schema.exchange.movement.v1.RecipientInfoType;
import fish.focus.schema.exchange.movement.v1.SendMovementToPluginType;
import fish.focus.schema.exchange.movement.v1.SetReportMovementType;
import fish.focus.schema.exchange.plugin.types.v1.EmailType;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.plugin.types.v1.PollType;
import fish.focus.schema.exchange.registry.v1.ExchangeRegistryMethod;
import fish.focus.schema.exchange.registry.v1.RegisterServiceRequest;
import fish.focus.schema.exchange.registry.v1.UnregisterServiceRequest;
import fish.focus.schema.exchange.service.v1.CapabilityListType;
import fish.focus.schema.exchange.service.v1.ServiceType;
import fish.focus.schema.exchange.service.v1.SettingListType;
import fish.focus.schema.exchange.service.v1.SettingType;
import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.schema.exchange.v1.TypeRefType;
import fish.focus.uvms.commons.date.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExchangeModuleRequestMapper {

    private static final String FLUX_VESSEL_PLUGIN = "flux-vessel-plugin";

    public static String createRegisterServiceRequest(ServiceType serviceType, CapabilityListType capabilityList, SettingListType settingList) {
        RegisterServiceRequest request = new RegisterServiceRequest();
        request.setMethod(ExchangeRegistryMethod.REGISTER_SERVICE);
        request.setService(serviceType);
        request.setCapabilityList(capabilityList);
        request.setSettingList(settingList);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createUnregisterServiceRequest(ServiceType serviceType) {
        UnregisterServiceRequest request = new UnregisterServiceRequest();
        request.setMethod(ExchangeRegistryMethod.UNREGISTER_SERVICE);
        request.setService(serviceType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSetMovementReportRequest(SetReportMovementType reportType, String username) {
        SetMovementReportRequest request = new SetMovementReportRequest();
        request.setMethod(ExchangeModuleMethod.SET_MOVEMENT_REPORT);
        request.setUsername(username);
        request.setRequest(reportType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSetMovementReportRequest(SetReportMovementType message, String username, String fluxDFValue, Instant date,
                                                        PluginType pluginType, String senderReceiver, String onValue) {
        SetMovementReportRequest request = new SetMovementReportRequest();
        request.setMethod(ExchangeModuleMethod.SET_MOVEMENT_REPORT);
        request.setUsername(username);
        request.setRequest(message);
        request.setDate(Date.from(date));
        populateBaseProperties(request, fluxDFValue, date, null, pluginType, senderReceiver, onValue, username, null, null, "");
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }


    public static String createReceiveAssetInformation(String assets, String username, PluginType pluginType, String source) {
        ReceiveAssetInformationRequest request = new ReceiveAssetInformationRequest();
        request.setAssets(assets);
        request.setUsername(username);
        request.setMethod(RECEIVE_ASSET_INFORMATION);
        request.setSenderOrReceiver(source);
        request.setDate(new Date());
        request.setPluginType(pluginType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSendAssetInformation(String assets, String username) {
        SendAssetInformationRequest request = new SendAssetInformationRequest();
        request.setAssets(assets);
        request.setUsername(username);
        request.setMethod(SEND_ASSET_INFORMATION);
        request.setSenderOrReceiver(FLUX_VESSEL_PLUGIN);
        request.setDate(new Date());
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createQueryAssetInformation(String assets, String username) {
        QueryAssetInformationRequest request = new QueryAssetInformationRequest();
        request.setAssets(assets);
        request.setUsername(username);
        request.setMethod(QUERY_ASSET_INFORMATION);
        request.setSenderOrReceiver(FLUX_VESSEL_PLUGIN);
        request.setDate(new Date());
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createReceiveSalesReportRequest(String report, String reportGuid, String sender, String username, PluginType typeOfOriginatingPlugin, Instant dateReceived, String on) {
        ReceiveSalesReportRequest request = new ReceiveSalesReportRequest();
        request.setReport(report);

        enrichBaseRequest(request, ExchangeModuleMethod.RECEIVE_SALES_REPORT, reportGuid, null, sender, dateReceived, username, typeOfOriginatingPlugin, on);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    @Deprecated
    public static String createReceiveSalesReportRequest(String report, String reportGuid, String sender, String username, PluginType typeOfOriginatingPlugin, Instant dateReceived) {
        return createReceiveSalesReportRequest(report, reportGuid, sender, username, typeOfOriginatingPlugin, dateReceived, null);
    }

    public static String createReceiveSalesQueryRequest(String query, String queryGuid, String sender, Instant receiveDate, String username, PluginType typeOfOriginatingPlugin, String on) {
        ReceiveSalesQueryRequest request = new ReceiveSalesQueryRequest();
        request.setQuery(query);
        enrichBaseRequest(request, ExchangeModuleMethod.RECEIVE_SALES_QUERY, queryGuid, null, sender, receiveDate, username, typeOfOriginatingPlugin, on);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    @Deprecated
    public static String createReceiveSalesQueryRequest(String query, String queryGuid, String sender, Instant receiveDate, String username, PluginType typeOfOriginatingPlugin) {
        return createReceiveSalesQueryRequest(query, queryGuid, sender, receiveDate, username, typeOfOriginatingPlugin, null);
    }

    public static String createReceiveSalesResponseRequest(String response, String guid, String sender, Instant date, String username, PluginType pluginType, String on) {
        ReceiveSalesResponseRequest receiveSalesResponseRequest = new ReceiveSalesResponseRequest();
        receiveSalesResponseRequest.setResponse(response);

        enrichBaseRequest(receiveSalesResponseRequest, ExchangeModuleMethod.RECEIVE_SALES_RESPONSE, guid, null, sender, date, username, pluginType, on);
        return JAXBMarshaller.marshallJaxBObjectToString(receiveSalesResponseRequest);
    }

    @Deprecated
    public static String createReceiveSalesResponseRequest(String response, String guid, String sender, Instant date, String username, PluginType pluginType) {
        return createReceiveSalesResponseRequest(response, guid, sender, date, username, pluginType, null);
    }

    public static String createReceiveInvalidSalesMessage(String respondToInvalidMessageRequest, String guid, String sender, Instant date, String username, PluginType pluginType) {
        return createReceiveInvalidSalesMessage(respondToInvalidMessageRequest, guid, sender,
                date, username, pluginType, null);
    }

    public static String createReceiveInvalidSalesMessage(String respondToInvalidMessageRequest, String guid, String sender, Instant date, String username, PluginType pluginType, String originalMessage) {
        ReceiveInvalidSalesMessage receiveInvalidSalesMessage = new ReceiveInvalidSalesMessage();
        receiveInvalidSalesMessage.setRespondToInvalidMessageRequest(respondToInvalidMessageRequest);
        receiveInvalidSalesMessage.setOriginalMessage(originalMessage);
        enrichBaseRequest(receiveInvalidSalesMessage, ExchangeModuleMethod.RECEIVE_INVALID_SALES_MESSAGE, guid, null, sender, date, username, pluginType, null);

        return JAXBMarshaller.marshallJaxBObjectToString(receiveInvalidSalesMessage);
    }


    /**
     @deprecated use createSendSalesResponseRequest(String response,
     String guid, String dataFlow,
     String senderOrReceiver, Date date,
     ExchangeLogStatusTypeType validationStatus,
     PluginType typeOfOriginatingPlugin) throws ExchangeModelMarshallException instead
     **/
    @Deprecated
    public static String createSendSalesResponseRequest(String response,
                                                        String guid, String dataFlow,
                                                        String receiver, Instant date,
                                                        ExchangeLogStatusTypeType validationStatus) {
        return createSendSalesResponseRequest(response, guid, dataFlow, receiver, date, validationStatus, PluginType.FLUX);
    }

    public static String createSendSalesResponseRequest(String response,
                                                        String guid, String dataFlow,
                                                        String receiver, Instant date,
                                                        ExchangeLogStatusTypeType validationStatus,
                                                        String typeOfOriginatingPlugin) {
        return createSendSalesResponseRequest(response, guid, dataFlow, receiver, date, validationStatus, PluginType.valueOf(typeOfOriginatingPlugin));
    }

    public static String createSendSalesResponseRequest(String response,
                                                        String guid, String dataFlow,
                                                        String receiver, Instant date,
                                                        ExchangeLogStatusTypeType validationStatus,
                                                        PluginType typeOfOriginatingPlugin) {
        SendSalesResponseRequest sendSalesResponseRequest = new SendSalesResponseRequest();
        sendSalesResponseRequest.setResponse(checkNotNull(response));
        sendSalesResponseRequest.setValidationStatus(validationStatus);

        enrichBaseRequest(sendSalesResponseRequest, ExchangeModuleMethod.SEND_SALES_RESPONSE, guid, dataFlow, receiver, date, null, typeOfOriginatingPlugin, null);
       return JAXBMarshaller.marshallJaxBObjectToString(sendSalesResponseRequest);
    }

    /**
     @deprecated use createSendSalesReportRequest(String report,
     String guid, String dataFlow,
     String receiver, Date date,
     ExchangeLogStatusTypeType validationStatus,
     PluginType typeOfOriginatingPlugin) throws ExchangeModelMarshallException instead
     **/
    @Deprecated
    public static String createSendSalesReportRequest(String report, String guid, String dataFlow, String receiver,
                                                      Instant date, ExchangeLogStatusTypeType validationStatus) {
        return createSendSalesReportRequest(report, guid, dataFlow, receiver, date, validationStatus, PluginType.FLUX);
    }

    public static String createSendSalesReportRequest(String report, String guid, String dataFlow, String receiver,
                                                      Instant date, ExchangeLogStatusTypeType validationStatus,
                                                      String typeOfOriginatingPlugin) {
        return createSendSalesReportRequest(report, guid, dataFlow, receiver, date, validationStatus, PluginType.valueOf(typeOfOriginatingPlugin));
    }

    public static String createSendSalesReportRequest(String report, String guid, String dataFlow, String receiver,
                                                      Instant date, ExchangeLogStatusTypeType validationStatus,
                                                      PluginType typeOfOriginatingPlugin) {
        SendSalesReportRequest sendSalesReportRequest = new SendSalesReportRequest();
        sendSalesReportRequest.setReport(checkNotNull(report));
        sendSalesReportRequest.setValidationStatus(validationStatus);

        enrichBaseRequest(sendSalesReportRequest, ExchangeModuleMethod.SEND_SALES_REPORT, guid, dataFlow, receiver, date, null, typeOfOriginatingPlugin, null);
        return JAXBMarshaller.marshallJaxBObjectToString(sendSalesReportRequest);
    }


    public static String createSendReportToPlugin(String pluginName, PluginType type, Instant fwdDate, String fwdRule, String recipient, MovementType payload, List<RecipientInfoType> recipientInfoList, String assetName, String ircs, String mmsi, String externalMarking, String flagState) {
        SendMovementToPluginRequest request = createSendReportToPluginRequest(pluginName, type, fwdDate, fwdRule, recipient, payload, recipientInfoList, assetName, ircs, mmsi, externalMarking, flagState);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    private static SendMovementToPluginRequest createSendReportToPluginRequest(String pluginName, PluginType type, Instant fwdDate, String fwdRule, String recipient, MovementType payload, List<RecipientInfoType> recipientInfoList, String assetName, String ircs, String mmsi, String externalMarking, String flagState) {
        SendMovementToPluginRequest request = new SendMovementToPluginRequest();
        request.setMethod(ExchangeModuleMethod.SEND_REPORT_TO_PLUGIN);
        SendMovementToPluginType sendMovementToPluginType = createSendMovementToPluginType(pluginName, type, fwdDate, fwdRule, recipient, payload, recipientInfoList, assetName, ircs, mmsi, externalMarking, flagState);
        request.setReport(sendMovementToPluginType);
        request.setUsername("UVMS");
        return request;
    }

    public static SendMovementToPluginType createSendMovementToPluginType(String pluginName, PluginType type, Instant fwdDate, String fwdRule, String recipient, MovementType payload, List<RecipientInfoType> recipientInfoList, String assetName, String ircs, String mmsi, String externalMarking, String flagState) {
        SendMovementToPluginType report = new SendMovementToPluginType();
        mapToMovementType(payload, ircs, mmsi, externalMarking, flagState, assetName);
        report.setTimestamp(Date.from(DateUtils.nowUTC()));
        report.setFwdDate(Date.from(fwdDate));
        report.setFwdRule(fwdRule);
        report.setRecipient(recipient);
        report.getRecipientInfo().addAll(recipientInfoList);
        report.setAssetName(assetName);
        report.setMovement(payload);
        report.setPluginType(type);
        report.setPluginName(pluginName);
        report.setIrcs(payload.getIrcs());
        return report;
    }

    private static void mapToMovementType(MovementType movementType, String ircs, String mmsi, String externalMarking, String flagState, String assetName) {
        movementType.setMmsi(mmsi);
        movementType.setExternalMarking(externalMarking);
        movementType.setIrcs(ircs);
        movementType.setFlagState(flagState);
        movementType.setAssetName(assetName);
    }

    public static String createSetCommandSendPollRequest(String pluginName, PollType poll, String username, String fwdRule) {
        SetCommandRequest request = createSetCommandRequest(pluginName, CommandTypeType.POLL, username, fwdRule);
        request.getCommand().setPoll(poll);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createMarshalledSetCommandSendEmailRequest(String pluginName, EmailType email, String fwdRule) {
        SetCommandRequest request = createSetCommandRequest(pluginName, CommandTypeType.EMAIL, "UVMS", fwdRule);
        request.getCommand().setEmail(email);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static SetCommandRequest createSetCommandSendEmailRequest(String pluginName, EmailType email, String fwdRule) {
        SetCommandRequest request = createSetCommandRequest(pluginName, CommandTypeType.EMAIL, "UVMS", fwdRule);
        request.getCommand().setEmail(email);
        return request;
    }

    private static SetCommandRequest createSetCommandRequest(String pluginName, CommandTypeType type, String username, String fwdRule) {
        SetCommandRequest request = new SetCommandRequest();
        request.setMethod(ExchangeModuleMethod.SET_COMMAND);
        CommandType commandType = new CommandType();
        commandType.setTimestamp(Date.from(DateUtils.nowUTC()));
        commandType.setCommand(type);
        commandType.setPluginName(pluginName);
        commandType.setFwdRule(fwdRule);
        request.setUsername(username);
        request.setCommand(commandType);
        return request;
    }

    public static String createGetServiceListRequest(List<PluginType> pluginTypes) {
        GetServiceListRequest request = new GetServiceListRequest();
        request.setMethod(ExchangeModuleMethod.LIST_SERVICES);
        request.getType().addAll(pluginTypes);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createUpdatePluginSettingRequest(String serviceClassName, String settingKey, String settingValue, String username) {
        UpdatePluginSettingRequest request = new UpdatePluginSettingRequest();
        request.setMethod(ExchangeModuleMethod.UPDATE_PLUGIN_SETTING);
        request.setServiceClassName(serviceClassName);
        request.setUsername(username);
        SettingType setting = new SettingType();
        setting.setKey(settingKey);
        setting.setValue(settingValue);
        request.setSetting(setting);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    // Asynch processed movement response
    public static String mapToProcessedMovementResponse(String username, MovementRefType movementRef) {
        ProcessedMovementResponse response = new ProcessedMovementResponse();
        response.setMethod(ExchangeModuleMethod.PROCESSED_MOVEMENT);
        response.setMovementRefType(movementRef);
        response.setUsername(username);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String createFluxMdrSyncEntityRequest(String reportType, String username, String fr) {
        SetFLUXMDRSyncMessageExchangeRequest request = new SetFLUXMDRSyncMessageExchangeRequest();
        request.setMethod(ExchangeModuleMethod.SET_MDR_SYNC_MESSAGE_REQUEST);
        request.setUsername(username);
        request.setRequest(reportType);
        request.setFr(fr);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFluxMdrSyncEntityResponse(String reportType, String username) {
        SetFLUXMDRSyncMessageExchangeResponse request = new SetFLUXMDRSyncMessageExchangeResponse();
        request.setMethod(ExchangeModuleMethod.SET_MDR_SYNC_MESSAGE_RESPONSE);
        request.setUsername(username);
        request.setRequest(reportType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    /**
     * @deprecated  As of release 4.0.2, replaced by createActivityRequest(String message, String username, String fluxDFValue,Date date,
     *                                              String messageGuid, PluginType pluginType, String senderReceiver, String onValue)
     */
    @Deprecated
    public static String createFluxFAReportRequest(String message, String username, String fluxDFValue,Instant date, String messageGuid,PluginType pluginType,String senderReceiver) {
        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
        request.setUsername(username);
        request.setRequest(message);
        request.setFluxDataFlow(fluxDFValue);
        request.setDate(Date.from(date));
        request.setMessageGuid(messageGuid);
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(senderReceiver);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFluxFAReportRequest(String message, String username, String fluxDFValue, Instant date,
                                                   String messageGuid, PluginType pluginType, String senderReceiver, String onValue,
                                                   String todt, String to, String ad) {
        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
        request.setRequest(message);
        populateBaseProperties(request, fluxDFValue, date, messageGuid, pluginType, senderReceiver, onValue, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFARequestForUnknownType(String message, String username, String fluxDFValue, Instant date,
                                                       String messageGuid, PluginType pluginType, String senderReceiver, String onValue,
                                                       String todt, String to, String ad) {
        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        request.setMethod(ExchangeModuleMethod.UNKNOWN);
        request.setRequest(message);
        populateBaseProperties(request, fluxDFValue, date, messageGuid, pluginType, senderReceiver, onValue, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFaQueryRequest(String message, String username, String fluxDFValue, Instant date,
                                              String messageGuid, PluginType pluginType, String senderReceiver, String onValue,
                                              String todt, String to, String ad) {
        SetFAQueryMessageRequest request = new SetFAQueryMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FA_QUERY_MESSAGE);
        request.setRequest(message);
        populateBaseProperties(request, fluxDFValue, date, messageGuid, pluginType, senderReceiver, onValue, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFluxResponseRequest(String message, String username, String dfValue, Instant date,
                                                   String messageGuid, PluginType pluginType, String senderReceiver, String onValue,
                                                   String todt, String to, String ad) {
        RcvFLUXFaResponseMessageRequest request = new RcvFLUXFaResponseMessageRequest();
        request.setMethod(ExchangeModuleMethod.RCV_FLUX_FA_RESPONSE_MESSAGE);
        request.setRequest(message);
        request.setResponseMessageGuid(messageGuid);
        populateBaseProperties(request, dfValue, date, messageGuid, pluginType, senderReceiver, onValue, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSendFaQueryMessageRequest(String faQueryMessageStr, String username, String logId, String fluxDataFlow,
                                                         String senderOrReceiver, String todt, String to, String ad, PluginType pluginType) {
        SetFAQueryMessageRequest request = new SetFAQueryMessageRequest();
        request.setMethod(ExchangeModuleMethod.SEND_FA_QUERY_MESSAGE);
        request.setRequest(faQueryMessageStr);
        populateBaseProperties(request, fluxDataFlow, Instant.now(), logId, pluginType, senderOrReceiver, null, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSendFaReportMessageRequest(String faReportMessageStr, String username, String logId, String fluxDataFlow,
                                                         String senderOrReceiver, String onValue, String todt, String to, String ad, PluginType pluginType) {
        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        request.setMethod(ExchangeModuleMethod.SEND_FLUX_FA_REPORT_MESSAGE);
        request.setRequest(faReportMessageStr);
        populateBaseProperties(request, fluxDataFlow, Instant.now(), logId, pluginType, senderOrReceiver, onValue, username, todt, to, ad);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    private static void populateBaseProperties(ExchangeBaseRequest request, String fluxDFValue, Instant date, String messageGuid, PluginType pluginType,
                                               String senderReceiver, String onValue, String username,  String todt, String to, String ad) {
        request.setUsername(username);
        request.setFluxDataFlow(fluxDFValue);
        request.setDate(Date.from(date));
        request.setMessageGuid(messageGuid);
        request.setPluginType(pluginType);
        request.setTodt(todt);
        request.setTo(to);
        request.setSenderOrReceiver(senderReceiver);
        request.setOnValue(onValue);
        request.setAd(ad);
    }


    public static String createFluxFAResponseRequest(String response, String username, String df, String messageGuid, String fr, ExchangeLogStatusTypeType status, String destination) {
        return createFluxFAResponseRequest(response, username, df, messageGuid, fr, status, destination, PluginType.FLUX);
    }

    public static String createFluxFAResponseRequestWithOnValue(String response, String username, String df, String messageGuid, String fr,
                                                                String onVal, ExchangeLogStatusTypeType status, String destination, PluginType pluginType,
                                                                String responseGuid) {
        SetFLUXFAResponseMessageRequest request = new SetFLUXFAResponseMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_RESPONSE_MESSAGE);
        request.setUsername(username);
        request.setRequest(response);
        request.setFluxDataFlow(df);
        request.setMessageGuid(messageGuid);
        request.setDate(Date.from(DateUtils.nowUTC()));
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(fr);
        request.setStatus(status);
        request.setDestination(destination);
        request.setOnValue(onVal);
        request.setResponseMessageGuid(responseGuid);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    /**
     *
     *@Deprecated Use the createFluxFAResponseRequestWithOnValue(...){} method instead
     */
    @Deprecated
    public static String createFluxFAResponseRequest(String response, String username, String df, String messageGuid, String fr, ExchangeLogStatusTypeType status, String destination, PluginType pluginType) {
        SetFLUXFAResponseMessageRequest request = new SetFLUXFAResponseMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_RESPONSE_MESSAGE);
        request.setUsername(username);
        request.setRequest(response);
        request.setFluxDataFlow(df);
        request.setMessageGuid(messageGuid);
        request.setDate(Date.from(DateUtils.nowUTC()));
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(fr);
        request.setStatus(status);
        request.setDestination(destination);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFluxFAResponseRequest(String response, String username, String df, String messageGuid, String fr,
                                                     ExchangeLogStatusTypeType status, String destination, PluginType pluginType, String todt,
                                                     String to, String onValue) {
        SetFLUXFAResponseMessageRequest request = new SetFLUXFAResponseMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_RESPONSE_MESSAGE);
        request.setUsername(username);
        request.setRequest(response);
        request.setFluxDataFlow(df);
        request.setMessageGuid(messageGuid);
        request.setDate(Date.from(DateUtils.nowUTC()));
        request.setTodt(todt);
        request.setTo(to);
        request.setOnValue(onValue);
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(fr);
        request.setStatus(status);
        request.setDestination(destination);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createFluxFAManualResponseRequest(String reportType, String username) {
        SetFLUXFAReportMessageRequest request = new SetFLUXFAReportMessageRequest();
        request.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
        request.setUsername(username);
        request.setRequest(reportType);
        request.setPluginType(PluginType.MANUAL);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createUpdateLogStatusRequest(String logGuid, ExchangeLogStatusTypeType newStatus) {
        UpdateLogStatusRequest request = new UpdateLogStatusRequest();
        request.setMethod(ExchangeModuleMethod.UPDATE_LOG_STATUS);
        request.setLogGuid(logGuid);
        request.setNewStatus(newStatus);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createUpdateLogStatusRequest(String logGuid, Exception e) {
        UpdateLogStatusRequest request = new UpdateLogStatusRequest();
        request.setMethod(ExchangeModuleMethod.UPDATE_LOG_BUSINESS_ERROR);
        request.setLogGuid(logGuid);
        if (e != null){
            request.setBusinessModuleExceptionMessage(ExceptionUtils.getMessage(e) + ":" + ExceptionUtils.getStackTrace(e));
        }
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createLogRefIdByTypeExistsRequest(String refGuid, List<TypeRefType> refTypes) {
        LogRefIdByTypeExistsRequest request = new LogRefIdByTypeExistsRequest();
        request.setMethod(ExchangeModuleMethod.LOG_REF_ID_BY_TYPE_EXISTS);
        request.setRefGuid(refGuid);
        request.getRefTypes().addAll(refTypes);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createLogIdByTypeExistsRequest(String id, TypeRefType refType) {
        LogIdByTypeExistsRequest request = new LogIdByTypeExistsRequest();
        request.setMethod(ExchangeModuleMethod.LOG_ID_BY_TYPE_EXISTS);
        request.setMessageGuid(id);
        request.setRefType(refType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    private static void enrichBaseRequest(ExchangeBaseRequest exchangeBaseRequest, ExchangeModuleMethod method, String guid, String dataFlow,
                                          String senderOrReceiver, Instant date, String username, PluginType pluginType, String on) {
        exchangeBaseRequest.setMethod(checkNotNull(method));
        exchangeBaseRequest.setDate(checkNotNull(Date.from(date)));
        exchangeBaseRequest.setMessageGuid(checkNotNull(guid));
        exchangeBaseRequest.setFluxDataFlow(dataFlow);
        exchangeBaseRequest.setSenderOrReceiver(checkNotNull(senderOrReceiver));
        exchangeBaseRequest.setUsername(username);
        exchangeBaseRequest.setPluginType(pluginType);
        exchangeBaseRequest.setOnValue(on);
    }

    /**
     * Ensures that an object reference is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    private static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException("Object " + reference + " is null");
        }
        return reference;
    }

    public static String createSetFLUXMovementReportRequest(String message, String username, String fluxDFValue, Instant date,
                                                            String messageGuid, PluginType pluginType, String senderReceiver, String onValue,
                                                            String guid, String registeredClassName, String ad, String to, String todt) throws JAXBException {
        SetFLUXMovementReportRequest request = new SetFLUXMovementReportRequest();
        request.setMethod(ExchangeModuleMethod.RECEIVE_MOVEMENT_REPORT_BATCH);
        request.setRequest(message);
        request.setMessageGuid(guid);
        populateBaseProperties(request, fluxDFValue, date, messageGuid, pluginType, senderReceiver, onValue,
                username, registeredClassName, ad, to, todt);
        return JAXBMarshaller.marshallJaxBObjectToString(request, "Unicode", true);
    }

    private static void populateBaseProperties(ExchangeBaseRequest request, String fluxDFValue, Instant date, String messageGuid,
                                               PluginType pluginType, String senderReceiver, String onValue, String username,
                                               String registeredClassName, String ad, String to, String todt) {
        request.setUsername(username);
        request.setFluxDataFlow(fluxDFValue);
        request.setDf(fluxDFValue);
        request.setDate(Date.from(date));
        request.setMessageGuid(messageGuid);
        request.setPluginType(pluginType);
        request.setSenderOrReceiver(senderReceiver);
        request.setOnValue(onValue);
        request.setRegisteredClassName(registeredClassName);
        request.setTo(to);
        request.setTodt(todt);
        request.setAd(ad);
    }


}
