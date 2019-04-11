package eu.europa.ec.fisheries.uvms.exchange.rest.unsecured;

import eu.europa.ec.fisheries.schema.exchange.module.v1.GetServiceListRequest;
import eu.europa.ec.fisheries.schema.exchange.module.v1.GetServiceListResponse;
import eu.europa.ec.fisheries.schema.exchange.module.v1.SetCommandRequest;
import eu.europa.ec.fisheries.uvms.exchange.bean.ServiceRegistryModelBean;
import eu.europa.ec.fisheries.uvms.exchange.entity.serviceregistry.Service;
import eu.europa.ec.fisheries.uvms.exchange.mapper.ServiceMapper;
import eu.europa.ec.fisheries.uvms.exchange.service.bean.ExchangeEventOutgoingServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api")
@Stateless
public class ExchangeAPIRestResource {

    final static Logger LOG = LoggerFactory
            .getLogger(ExchangeAPIRestResource.class);


    @Inject
    private ServiceRegistryModelBean serviceRegistryModel;

    @EJB
    private ExchangeEventOutgoingServiceBean exchangeEventOutgoingService;

    /**
     *
     * @responseMessage 200 [Success]
     * @responseMessage 500 [Error]
     *
     * @summary Get a list of all registered and active services
     *
     */
    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/serviceList")
    public GetServiceListResponse getServiceList(GetServiceListRequest request) {
        GetServiceListResponse getServiceListResponse = new GetServiceListResponse();
        try {
            List<Service> serviceList = serviceRegistryModel.getPlugins(request.getType());
            getServiceListResponse.getService().addAll(ServiceMapper.toServiceModelList(serviceList));
            return getServiceListResponse;
        } catch (Exception ex) {
            LOG.error("Call failed", ex);
        }
        return getServiceListResponse;
    }

    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/pluginCommand")
    public Response sendCommandToPlugin(SetCommandRequest request) {
        try {
            String validationResult = exchangeEventOutgoingService.sendCommandToPluginFromRest(request);
            if (validationResult.equals("OK")) {
                return Response.ok().build();
            }
            return Response.status(400, "Incomplete request").entity(validationResult).build();
        }catch (Exception e){
            LOG.error(e.getMessage(), e);
            return Response.status(500).entity(e).build();
        }
    }


}