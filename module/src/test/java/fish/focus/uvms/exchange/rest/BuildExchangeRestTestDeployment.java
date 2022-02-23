package fish.focus.uvms.exchange.rest;

import java.util.Arrays;
import javax.ejb.EJB;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.rest.security.UnionVMSFeature;
import fish.focus.uvms.usm.jwt.JwtTokenHandler;
import fish.focus.uvms.exchange.service.BuildExchangeServiceTestDeployment;

public abstract class BuildExchangeRestTestDeployment extends BuildExchangeServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildExchangeRestTestDeployment.class);
    
    @EJB
    private JwtTokenHandler tokenHandler;

    private String token;

    protected WebTarget getWebTarget() {

        Client client = ClientBuilder.newClient();
        client.register(JsonBConfigurator.class);
        return client.target("http://localhost:8080/exchangeservice/rest");
    }

    protected String getToken() {
        if (token == null) {
            token = tokenHandler.createToken("user", 
                    Arrays.asList(UnionVMSFeature.viewExchange.getFeatureId(), 
                            UnionVMSFeature.manageExchangeTransmissionStatuses.getFeatureId(),
                            UnionVMSFeature.manageExchangeSendingQueue.getFeatureId()));
        }
        return token;
    }

}
