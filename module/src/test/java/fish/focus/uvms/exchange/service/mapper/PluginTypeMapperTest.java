package fish.focus.uvms.exchange.service.mapper;

import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.uvms.exchange.service.mapper.PluginTypeMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluginTypeMapperTest {

    @Test
    public void mapWhenEmail() {
        assertMapping(PluginType.EMAIL, fish.focus.schema.movementrules.exchange.v1.PluginType.EMAIL);
    }

    @Test
    public void mapWhenFLUX() {
        assertMapping(PluginType.FLUX, fish.focus.schema.movementrules.exchange.v1.PluginType.FLUX);
    }

    @Test
    public void mapWhenSatelliteReceiver() {
        assertMapping(PluginType.SATELLITE_RECEIVER, fish.focus.schema.movementrules.exchange.v1.PluginType.SATELLITE_RECEIVER);
    }

    @Test
    public void mapWhenNAF() {
        assertMapping(PluginType.NAF, fish.focus.schema.movementrules.exchange.v1.PluginType.NAF);
    }

    @Test
    public void mapWhenOther() {
        assertMapping(PluginType.OTHER, fish.focus.schema.movementrules.exchange.v1.PluginType.OTHER);
    }

    @Test(expected = NullPointerException.class)
    public void mapWhenNull() {
        PluginTypeMapper.map(null);
    }

    private void assertMapping(PluginType input, fish.focus.schema.movementrules.exchange.v1.PluginType expectedOutput) {
        assertEquals(expectedOutput, PluginTypeMapper.map(input));
    }

}
