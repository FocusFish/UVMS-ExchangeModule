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
import fish.focus.schema.exchange.module.v1.GetServiceListResponse;
import fish.focus.schema.exchange.module.v1.SendMovementToPluginResponse;
import fish.focus.schema.exchange.module.v1.SetCommandResponse;
import fish.focus.schema.exchange.module.v1.UpdatePluginSettingResponse;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;

import java.util.List;

public class ExchangeModuleResponseMapper {

    public static AcknowledgeType mapAcknowledgeTypeOK() {
    	AcknowledgeType ackType = new AcknowledgeType();
    	ackType.setType(AcknowledgeTypeType.OK);
    	return ackType;
    }

    public static AcknowledgeType mapAcknowledgeTypeNOK(String logId, String errorMessage) {
    	AcknowledgeType ackType = new AcknowledgeType();
    	ackType.setMessage(errorMessage);
    	ackType.setLogId(logId);
    	ackType.setType(AcknowledgeTypeType.NOK);
    	return ackType;
    }
    
    public static String mapSetCommandResponse(AcknowledgeType ackType) {
        SetCommandResponse response = new SetCommandResponse();
        response.setResponse(ackType);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }
    
    public static String mapSendMovementToPluginResponse(AcknowledgeType ackType) {
    	SendMovementToPluginResponse response = new SendMovementToPluginResponse();
    	response.setResponse(ackType);
    	return JAXBMarshaller.marshallJaxBObjectToString(response);
	}
    
    public static String mapUpdateSettingResponse(AcknowledgeType ackType) {
    	UpdatePluginSettingResponse response = new UpdatePluginSettingResponse();
    	response.setResponse(ackType);
    	return JAXBMarshaller.marshallJaxBObjectToString(response);
    }
    

	public static String mapServiceListResponse(List<ServiceResponseType> serviceList) {
		GetServiceListResponse response = new GetServiceListResponse();
		response.getService().addAll(serviceList);
		return JAXBMarshaller.marshallJaxBObjectToString(response);
	}

}