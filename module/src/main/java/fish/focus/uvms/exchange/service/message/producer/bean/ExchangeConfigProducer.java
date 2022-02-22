package fish.focus.uvms.exchange.service.message.producer.bean;

import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.impl.AbstractProducer;
import fish.focus.uvms.config.message.ConfigMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

@Stateless
@LocalBean
public class ExchangeConfigProducer extends AbstractProducer implements ConfigMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeConfigProducer.class);

    @Resource(mappedName =  "java:/" + MessageConstants.QUEUE_CONFIG)
    private Queue destination;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE)
    private Queue replyToQueue;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendConfigMessage(String text) {
        try{
            return sendModuleMessage(text, replyToQueue);
        } catch (JMSException e) {
            LOG.error("[ Error when sending Asset info message. ]", e);
            throw new RuntimeException("Error when sending asset info message.", e);
        }
    }

    @Override
    public Destination getDestination() {
        return destination;
    }
}