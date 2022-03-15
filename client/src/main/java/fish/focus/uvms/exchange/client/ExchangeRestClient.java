package fish.focus.uvms.exchange.client;

import fish.focus.schema.exchange.module.v1.GetServiceListRequest;
import fish.focus.schema.exchange.module.v1.GetServiceListResponse;
import fish.focus.schema.exchange.module.v1.SetCommandRequest;
import fish.focus.schema.exchange.plugin.types.v1.EmailType;
import fish.focus.schema.exchange.v1.ExchangeLogStatusType;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.rest.security.InternalRestTokenHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@RequestScoped
public class ExchangeRestClient {

    private WebTarget webTarget;
    
    @Resource(name = "java:global/exchange_endpoint")
    private String exchangeEndpoint;

    @Inject
    private InternalRestTokenHandler internalRestTokenHandler;

    @PostConstruct
    public void initClient() {
        String url = exchangeEndpoint + "/unsecured/api/";

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        Client client = clientBuilder.build();

        client.register(JsonBConfigurator.class);
        webTarget = client.target(url);
    }



    public GetServiceListResponse getServiceList(GetServiceListRequest request) {

        Response response = webTarget
                .path("serviceList")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.json(request), Response.class);

        if(response.getStatus() != 200) {
            throw new RuntimeException("Errormessage from exchange: " + response.readEntity(String.class));
        }
        return response.readEntity(GetServiceListResponse.class);
    }

    public void sendEmail(EmailType email) {

        Response response = webTarget
                .path("sendEmail")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.json(email));

        if(response.getStatus() != 200) {
            throw new RuntimeException("Errormessage from exchange: " + response.readEntity(String.class));
        }
    }

    public void sendCommandToPlugin(SetCommandRequest request) {

        Response response = webTarget
                .path("pluginCommand")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.json(request));

        if(response.getStatus() != 200) {
            throw new RuntimeException("Errormessage from exchange: " + response.readEntity(String.class));
        }
    }

    public ExchangeLogStatusType getPollStatus(String uuid) {

        Response response = webTarget
                .path("poll")
                .path(uuid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .get(Response.class);

        if(response.getStatus() != 200) {
            throw new RuntimeException("Errormessage from exchange: " + response.readEntity(String.class));
        }
        if(response.getLength() <= 0){
            return null;
        }
        return response.readEntity(ExchangeLogStatusType.class);
    }


}
