package fish.focus.uvms.exchange.model.mapper;

import static org.junit.Assert.assertEquals;

import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import org.junit.Test;

public class ExchangeModuleRequestMapperTest {

    @Test
    public void createUpdateLogStatusRequest() throws Exception {
        String logGuid = "abc";
        ExchangeLogStatusTypeType newStatus = ExchangeLogStatusTypeType.SUCCESSFUL;

        String result = ExchangeModuleRequestMapper.createUpdateLogStatusRequest(logGuid, newStatus);
        assertEquals("<ns2:UpdateLogStatusRequest xmlns:ns2=\"urn:module.exchange.schema.focus.fish:v1\">\n" + "    <method>UPDATE_LOG_STATUS</method>\n" + "    <logGuid>abc</logGuid>\n" + "    <newStatus>SUCCESSFUL</newStatus>\n" + "</ns2:UpdateLogStatusRequest>", result);
    }

}