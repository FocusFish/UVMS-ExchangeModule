package fish.focus.uvms.exchange.service.mapper;

import fish.focus.schema.exchange.plugin.types.v1.PluginType;

public class PluginTypeMapper {

    public static eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType map(PluginType pluginType) {
        switch (pluginType) {
            case EMAIL:
                return eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType.EMAIL;
            case FLUX:
                return eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType.FLUX;
            case SATELLITE_RECEIVER:
                return eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType.SATELLITE_RECEIVER;
            case NAF:
                return eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType.NAF;
            case OTHER:
            default:
                return eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType.OTHER;
        }
    }

}