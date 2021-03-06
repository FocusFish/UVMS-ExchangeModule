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

import fish.focus.schema.exchange.common.v1.AcknowledgeType;
import fish.focus.schema.exchange.common.v1.AcknowledgeTypeType;
import fish.focus.schema.exchange.common.v1.PollStatusAcknowledgeType;
import fish.focus.schema.exchange.plugin.v1.AcknowledgeResponse;
import fish.focus.schema.exchange.plugin.v1.ExchangePluginMethod;
import fish.focus.schema.exchange.plugin.v1.PingResponse;
import fish.focus.schema.exchange.registry.v1.ExchangeRegistryMethod;
import fish.focus.schema.exchange.registry.v1.RegisterServiceResponse;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;
import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;

/**
 **/
public class ExchangePluginResponseMapper {

    public static AcknowledgeType mapToAcknowledgeType(AcknowledgeTypeType ackType) {
        AcknowledgeType type = new AcknowledgeType();
        type.setType(ackType);
        return type;
    }

    public static AcknowledgeType mapToAcknowledgeType(String logId, AcknowledgeTypeType ackType) {
        AcknowledgeType type = new AcknowledgeType();
        type.setLogId(logId);
        type.setType(ackType);
        return type;
    }

    public static AcknowledgeType mapToAcknowledgeType(String logId, String unsentMessageGuid, AcknowledgeTypeType ackType) {
        AcknowledgeType type = new AcknowledgeType();
        type.setLogId(logId);
        type.setUnsentMessageGuid(unsentMessageGuid);
        type.setType(ackType);
        return type;
    }

    public static AcknowledgeType mapToAcknowledgeType(String logId, AcknowledgeTypeType ackType, String message) {
        AcknowledgeType type = mapToAcknowledgeType(logId, ackType);
        type.setMessage(message);
        return type;
    }

    public static AcknowledgeType mapToAcknowledgeType(String logId, String unsentMessageGuid, AcknowledgeTypeType ackType, String message) {
        AcknowledgeType type = mapToAcknowledgeType(logId, unsentMessageGuid, ackType);
        type.setMessage(message);
        return type;
    }

    public static String mapToRegisterServiceResponseOK(String messageId, ServiceResponseType service)  {
        RegisterServiceResponse response = mapToRegisterServiceResponse(messageId, AcknowledgeTypeType.OK);
        response.setService(service);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String mapToRegisterServiceResponseNOK(String messageId, String message) {
        RegisterServiceResponse response = mapToRegisterServiceResponse(messageId, AcknowledgeTypeType.NOK);
        response.getAck().setMessage(message);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    private static RegisterServiceResponse mapToRegisterServiceResponse(String messageId, AcknowledgeTypeType ackType) {
        RegisterServiceResponse response = new RegisterServiceResponse();
        response.setMethod(ExchangeRegistryMethod.REGISTER_SERVICE);
        response.setAck(mapToAcknowledgeType(messageId, ackType));
        return response;
    }

    public static String mapToPingResponse(boolean registered, boolean enabled) {
        PingResponse response = new PingResponse();
        response.setMethod(ExchangePluginMethod.PING);
        response.setResponse("pong");
        response.setRegistered(registered);
        response.setEnabled(enabled);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    private static String mapToAcknowledgeResponse(String serviceClassName, AcknowledgeType ackType, ExchangePluginMethod method) {
        AcknowledgeResponse response = new AcknowledgeResponse();
        response.setMethod(method);
        response.setServiceClassName(serviceClassName);
        response.setResponse(ackType);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }
    private static String mapToSetPollStatusAcknowledgeResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid, ExchangeLogStatusTypeType status, ExchangePluginMethod method) {
        AcknowledgeResponse response = new AcknowledgeResponse();
        PollStatusAcknowledgeType pollStatusAcknowledgeType = new PollStatusAcknowledgeType();
        response.setMethod(method);
        response.setServiceClassName(serviceClassName);
        response.setResponse(ackType);
        ackType.setPollStatus(pollStatusAcknowledgeType);
        pollStatusAcknowledgeType.setStatus(status);
        pollStatusAcknowledgeType.setPollId(pollGuid);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String mapToStopResponse(String serviceClassName, AcknowledgeType ackType) {
        return mapToAcknowledgeResponse(serviceClassName, ackType, ExchangePluginMethod.STOP);
    }

    public static String mapToStartResponse(String serviceClassName, AcknowledgeType ackType) {
        return mapToAcknowledgeResponse(serviceClassName, ackType, ExchangePluginMethod.START);
    }

    public static String mapToSetCommandResponse(String serviceClassName, AcknowledgeType ackType)  {
        return mapToAcknowledgeResponse(serviceClassName, ackType, ExchangePluginMethod.SET_COMMAND);
    }

    public static String mapToSetConfigResponse(String serviceClassName, AcknowledgeType ackType)  {
        return mapToAcknowledgeResponse(serviceClassName, ackType, ExchangePluginMethod.SET_CONFIG);
    }

    public static String mapToSetReportResponse(String serviceClassName, AcknowledgeType ackType)  {
        return mapToAcknowledgeResponse(serviceClassName, ackType, ExchangePluginMethod.SET_REPORT);
    }
    public static String mapToSetPollStatusToUnknownResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid) {
        return mapToSetPollStatusAcknowledgeResponse(serviceClassName, ackType, pollGuid, ExchangeLogStatusTypeType.UNKNOWN, ExchangePluginMethod.SET_COMMAND);
    }
    public static String mapToSetPollStatusToPendingResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid)  {
        return mapToSetPollStatusAcknowledgeResponse(serviceClassName, ackType, pollGuid, ExchangeLogStatusTypeType.PENDING, ExchangePluginMethod.SET_COMMAND);
    }

    public static String mapToSetPollStatusToTransmittedResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid)  {
        return mapToSetPollStatusAcknowledgeResponse(serviceClassName, ackType, pollGuid, ExchangeLogStatusTypeType.PROBABLY_TRANSMITTED, ExchangePluginMethod.SET_COMMAND);
    }

    public static String mapToSetPollStatusToSuccessfulResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid)  {
        return mapToSetPollStatusAcknowledgeResponse(serviceClassName, ackType, pollGuid, ExchangeLogStatusTypeType.SUCCESSFUL, ExchangePluginMethod.SET_COMMAND);
    }

    public static String mapToSetPollStatusToFailedResponse(String serviceClassName, AcknowledgeType ackType, String pollGuid)  {
        return mapToSetPollStatusAcknowledgeResponse(serviceClassName, ackType, pollGuid, ExchangeLogStatusTypeType.FAILED, ExchangePluginMethod.SET_COMMAND);
    }
}