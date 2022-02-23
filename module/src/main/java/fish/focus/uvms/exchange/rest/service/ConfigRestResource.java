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

import fish.focus.schema.exchange.v1.SearchField;
import fish.focus.uvms.rest.security.RequiresFeature;
import fish.focus.uvms.rest.security.UnionVMSFeature;
import fish.focus.uvms.exchange.rest.mock.ExchangeMock;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/config")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
@RequiresFeature(UnionVMSFeature.viewExchange)
public class ConfigRestResource {

    final static Logger LOG = LoggerFactory.getLogger(ConfigRestResource.class);

    @GET
    @Path(value = "/searchfields")
    public Response getConfigSearchFields() {
        try {
            return Response.ok(SearchField.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {}", e.getMessage());
            throw e;
        }
    }

    @GET
    @Path(value = "/")
    public Response getConfiguration() {
        try {
            Map<String, List> configuration = ExchangeMock.mockConfiguration();
            return Response.ok(configuration).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config configuration. ] {}", e.getMessage());
            throw e;
        }
    }
}