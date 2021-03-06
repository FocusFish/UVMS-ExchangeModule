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

import fish.focus.schema.exchange.common.v1.CommandType;
import fish.focus.schema.exchange.common.v1.ReportType;
import fish.focus.schema.exchange.common.v1.ReportTypeType;
import fish.focus.schema.exchange.movement.v1.SendMovementToPluginType;
import fish.focus.schema.exchange.plugin.v1.*;
import fish.focus.schema.exchange.service.v1.SettingListType;

import java.time.Instant;
import java.util.Date;  //leave be


/**
 **/
public class ExchangePluginRequestMapper {

    public static String createSetReportRequest(Instant dateReceived, SendMovementToPluginType movement, String unsentMessageGuid, String logId) {
        SetReportRequest request = new SetReportRequest();
        request.setMethod(ExchangePluginMethod.SET_REPORT);
        ReportType reportType = new ReportType();
        reportType.setRecipient(movement.getRecipient());
        reportType.getRecipientInfo().addAll(movement.getRecipientInfo());
        reportType.setMovement(movement.getMovement());
        reportType.setTimestamp(Date.from(dateReceived));
        reportType.setType(ReportTypeType.MOVEMENT);
        reportType.setUnsentMessageGuid(unsentMessageGuid);
        reportType.setLogId(logId);
        request.setReport(reportType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSetCommandRequest(CommandType commandType) {
        SetCommandRequest request = new SetCommandRequest();
        request.setMethod(ExchangePluginMethod.SET_COMMAND);
        request.setCommand(commandType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSetConfigRequest(SettingListType settingList) {
        SetConfigRequest request = new SetConfigRequest();
        request.setMethod(ExchangePluginMethod.SET_CONFIG);
        request.setConfigurations(settingList);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createPingRequest() {
        PingRequest request = new PingRequest();
        request.setMethod(ExchangePluginMethod.PING);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createStartRequest() {
        StartRequest request = new StartRequest();
        request.setMethod(ExchangePluginMethod.START);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createStopRequest() {
        StopRequest request = new StopRequest();
        request.setMethod(ExchangePluginMethod.STOP);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    /**
     * Maps the MDR Request that has to be sent to the MDR plugin to string.
     *
     * @param mdrBaseRequest
     * @return
     * @throws
     */
    public static String mapMdrRequestToPluginBaseRequest(String mdrBaseRequest) {
        SetMdrPluginRequest pluginRequest = new SetMdrPluginRequest();
        pluginRequest.setMethod(ExchangePluginMethod.SET_MDR_REQUEST);
        pluginRequest.setRequest(mdrBaseRequest);
        return JAXBMarshaller.marshallJaxBObjectToString(pluginRequest);
    }

    public static String createSetFLUXFAResponseRequestWithOn(String  fluxFAResponse, String destination, String df, String fr, String onValue) {
        SetFLUXFAResponseRequest request = new SetFLUXFAResponseRequest();
        request.setMethod(ExchangePluginMethod.SET_FLUX_RESPONSE);
        request.setResponse(fluxFAResponse);
        request.setDestination(destination);
        request.setFluxDataFlow(df);
        request.setSenderOrReceiver(fr);
        request.setOnValue(onValue);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    /**
     *
     *  @Deprecated Use createSetFLUXFAResponseRequestWithOn(..){} instead.
     */
    @Deprecated
    public static String createSetFLUXFAResponseRequest(String  fluxFAResponse, String destination, String df, String fr) {
        SetFLUXFAResponseRequest request = new SetFLUXFAResponseRequest();
        request.setMethod(ExchangePluginMethod.SET_FLUX_RESPONSE);
        request.setResponse(fluxFAResponse);
        request.setDestination(destination);
        request.setFluxDataFlow(df);
        request.setSenderOrReceiver(fr);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String createSendFLUXFAQueryRequest(String request, String destination, String df, String fr) {
        SetFLUXFAQueryRequest faqReq = new SetFLUXFAQueryRequest();
        faqReq.setMethod(ExchangePluginMethod.SEND_FA_QUERY);
        faqReq.setResponse(request);
        faqReq.setDestination(destination);
        faqReq.setFluxDataFlow(df);
        faqReq.setSenderOrReceiver(fr);
        return JAXBMarshaller.marshallJaxBObjectToString(faqReq);
    }

    public static String createSendFLUXFAReportRequest(String request, String destination, String df, String fr) {
        SetFLUXFAReportRequest faqReq = new SetFLUXFAReportRequest();
        faqReq.setMethod(ExchangePluginMethod.SEND_FA_REPORT);
        faqReq.setResponse(request);
        faqReq.setDestination(destination);
        faqReq.setFluxDataFlow(df);
        faqReq.setSenderOrReceiver(fr);
        return JAXBMarshaller.marshallJaxBObjectToString(faqReq);
    }
}