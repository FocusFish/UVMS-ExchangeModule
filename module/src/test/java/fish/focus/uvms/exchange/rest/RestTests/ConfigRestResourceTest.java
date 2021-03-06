package fish.focus.uvms.exchange.rest.RestTests;

import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.schema.exchange.v1.SearchField;
import fish.focus.schema.exchange.v1.SourceType;
import fish.focus.schema.exchange.v1.TypeRefType;
import fish.focus.uvms.exchange.rest.BuildExchangeRestTestDeployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ConfigRestResourceTest extends BuildExchangeRestTestDeployment {

    @Test
    @OperateOnDeployment("exchangeservice")
    public void getConfigSearchFields() throws Exception {
        String stringResponse = getWebTarget()
                .path("config")
                .path("searchfields")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        for (SearchField s: SearchField.values()) {
            assertTrue(stringResponse.contains(s.value()));
        }
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void getConfigurationTest() throws Exception {
        String stringResponse = getWebTarget()
                .path("config")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        for (ExchangeLogStatusTypeType e: ExchangeLogStatusTypeType.values()) {
            assertTrue(stringResponse.contains(e.value()));
        }
        for (TypeRefType e: TypeRefType.values()) {
            assertTrue(stringResponse.contains(e.value()));
        }
        for (SourceType e: SourceType.values()) {
            assertTrue(e.name(), stringResponse.contains(e.name()));
        }
    }

}
