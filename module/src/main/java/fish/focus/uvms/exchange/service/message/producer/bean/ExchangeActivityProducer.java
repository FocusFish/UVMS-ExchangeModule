package fish.focus.uvms.exchange.service.message.producer.bean;

import fish.focus.schema.exchange.module.v1.ExchangeModuleMethod;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.impl.AbstractProducer;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import static fish.focus.uvms.exchange.model.constant.ExchangeModelConstants.ACTIVITY_EVENT_QUEUE;
import java.util.Map;

@Stateless
public class ExchangeActivityProducer extends AbstractProducer {

    @Resource(mappedName = "java:/" + ACTIVITY_EVENT_QUEUE)
    private Queue destination;

    @Override
    public Destination getDestination() {
        return destination;
    }

    public void sendActivityMessage(String message) throws JMSException {
        sendModuleMessage(message, null);
    }

    public void sendEfrSaveActivity(String text) throws JMSException {
        final Map<String, String> jmsFunctionProperty = Map.of(MessageConstants.JMS_FUNCTION_PROPERTY, ExchangeModuleMethod.EFR_SAVE_ACTIVITY.name());
        sendModuleMessageWithProps(text, destination, jmsFunctionProperty);
    }
}
