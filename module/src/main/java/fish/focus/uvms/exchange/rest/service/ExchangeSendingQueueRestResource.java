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
package fish.focus.uvms.exchange.rest.service;

import fish.focus.schema.exchange.v1.UnsentMessageType;
import fish.focus.uvms.rest.security.RequiresFeature;
import fish.focus.uvms.rest.security.UnionVMSFeature;
import fish.focus.uvms.exchange.rest.dto.exchange.SendingGroupLog;
import fish.focus.uvms.exchange.rest.mapper.ExchangeLogMapper;
import fish.focus.uvms.exchange.service.bean.ExchangeLogServiceBean;
import fish.focus.uvms.exchange.service.dao.UnsentMessageDaoBean;
import fish.focus.uvms.exchange.service.entity.unsent.UnsentMessage;
import fish.focus.uvms.exchange.service.mapper.UnsentMessageMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sendingqueue")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class ExchangeSendingQueueRestResource {

    final static Logger LOG = LoggerFactory.getLogger(ExchangeSendingQueueRestResource.class);

    @EJB
    private ExchangeLogServiceBean serviceLayer;

    @Inject
    private UnsentMessageDaoBean unsentMessageDao;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewExchange)
    public Response getSendingQueue() {
        LOG.info("Get list invoked in rest layer");
        try {
            List<UnsentMessage> unsentMessageList = unsentMessageDao.getAll();
            List<UnsentMessageType> unsentMessageTypeList = UnsentMessageMapper.toModel(unsentMessageList);
            List<SendingGroupLog> sendingQueue = ExchangeLogMapper.mapToSendingQueue(unsentMessageTypeList);
            return Response.ok(sendingQueue).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting log list. ] {} ", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @PUT
    @Path("/send")
    @RequiresFeature(UnionVMSFeature.manageExchangeSendingQueue)
    public Response send(final List<String> messageIdList) {
        LOG.info("Get list invoked in rest layer:{}", messageIdList);
        try {
            //TODO swaggerize messageIdList
            serviceLayer.resend(messageIdList, request.getRemoteUser());
            return Response.ok(true).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting log list. {} ] {} ", messageIdList, ex.getMessage());
            throw ex;
        }
    }
}
