/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package fish.focus.uvms.exchange.service.message.producer.bean;

import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.impl.AbstractTopicProducer;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.exchange.service.message.event.PluginErrorEvent;
import fish.focus.uvms.exchange.service.message.event.carrier.PluginErrorEventCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

@Stateless
@LocalBean
public class ExchangeEventBusTopicProducer extends AbstractTopicProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeEventBusTopicProducer.class);

    @Resource(mappedName =  "java:/" + MessageConstants.EVENT_BUS_TOPIC)
    private Topic destination;

    @Resource(mappedName = "java:/jms/queue/UVMSExchangeEvent")
    private Queue replyToQueue;

    @Override
    public String sendEventBusMessage(String text, String serviceName) {
        try {
            LOG.debug("Sending event bus message from Exchange module to recipient om JMS Topic to: {} ", serviceName);
            return sendEventBusMessage(text, serviceName, replyToQueue);
        } catch (Exception e) {
            LOG.error("[ Error when sending message. ] ", e);
            throw new RuntimeException("[ Error when sending message. ]", e);
        }
    }

    public void sendPluginErrorResponseMessage(@Observes @PluginErrorEvent PluginErrorEventCarrier message) {
        try {
            String data = JAXBMarshaller.marshallJaxBObjectToString(message.getErrorFault());
            final String jmsMessageID = message.getJmsMessage().getJMSMessageID();
            final String serviceName = message.getServiceType() != null ? message.getServiceType() : "unknown";
            sendEventBusMessageWithSpecificIds(data, serviceName, null, null, jmsMessageID);
            LOG.debug("Sending error message back from Exchange module to recipient om JMS Topic with correlationID: {} ", jmsMessageID);
        } catch (Exception e) {
            LOG.error("Error when returning Error message to recipient", e);
        }
    }

    @Override
    public Destination getDestination() {
        return destination;
    }
}