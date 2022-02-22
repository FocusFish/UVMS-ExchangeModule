package fish.focus.uvms.exchange.client;

import fish.focus.schema.exchange.module.v1.GetServiceListRequest;
import fish.focus.schema.exchange.module.v1.GetServiceListResponse;
import fish.focus.schema.exchange.module.v1.SetCommandRequest;
import fish.focus.schema.exchange.plugin.types.v1.EmailType;
import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.v1.ExchangeLogStatusType;
import fish.focus.uvms.exchange.client.ExchangeRestClient;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ExchangeRestClientTest extends AbstractClientTest {

    @Inject
    ExchangeRestClient exchangeRestClient;
    
    @Before
    public void before() throws NamingException{
        InitialContext ctx = new InitialContext();
        ctx.rebind("java:global/exchange_endpoint", "http://localhost:8080/exchange/rest");
    }

    @Test
    @OperateOnDeployment("normal")
    public void getServiceListTest() {
        GetServiceListRequest request = new GetServiceListRequest();
        request.getType().add(PluginType.OTHER);
        request.getType().add(PluginType.BELGIAN_ACTIVITY);

        GetServiceListResponse serviceList = exchangeRestClient.getServiceList(request);
        assertNotNull(serviceList);
    }

    @Test
    @OperateOnDeployment("normal")
    public void sendEmailTest() {
        exchangeRestClient.sendEmail(new EmailType());  //just testing that we reach the endpoint

    }

    @Test
    @OperateOnDeployment("normal")
    public void sendCommandTest() {
        try {
            exchangeRestClient.sendCommandToPlugin(new SetCommandRequest());  //just testing that we reach the endpoint

        }catch (RuntimeException e){
            assertTrue(e.getMessage().startsWith("Errormessage from exchange:"));
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getPollStatusTest() {
        ExchangeLogStatusType pollStatus = exchangeRestClient.getPollStatus(UUID.randomUUID().toString());//just testing that we reach the endpoint
        assertNull(pollStatus);

    }

}
