package fish.focus.uvms.exchange.service.mapper;

import fish.focus.schema.exchange.plugin.types.v1.PluginType;

public class PluginTypeMapper {

    public static fish.focus.schema.movementrules.exchange.v1.PluginType map(PluginType pluginType) {
        switch (pluginType) {
            case EMAIL:
                return fish.focus.schema.movementrules.exchange.v1.PluginType.EMAIL;
            case FLUX:
                return fish.focus.schema.movementrules.exchange.v1.PluginType.FLUX;
            case SATELLITE_RECEIVER:
                return fish.focus.schema.movementrules.exchange.v1.PluginType.SATELLITE_RECEIVER;
            case NAF:
                return fish.focus.schema.movementrules.exchange.v1.PluginType.NAF;
            case OTHER:
            default:
                return fish.focus.schema.movementrules.exchange.v1.PluginType.OTHER;
        }
    }

}
