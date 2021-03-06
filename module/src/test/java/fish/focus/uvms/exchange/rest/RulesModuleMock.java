package fish.focus.uvms.exchange.rest;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import fish.focus.uvms.exchange.service.message.producer.bean.ExchangeEventProducer;

@MessageDriven(mappedName = "jms/queue/UVMSRulesEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "UVMSRulesEvent"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "messageSelector='ValidationResultsByRawGuid'")})
public class RulesModuleMock implements MessageListener {
    
    @Inject
    ExchangeEventProducer messageProducer;

    @Override
    public void onMessage(Message message) {
        try {
            /*ValidationMessageTypeResponse response = new ValidationMessageTypeResponse();
            ValidationMessageType validationMessageType = new ValidationMessageType();
            validationMessageType.setEntity("Rules Mock Entity");
            validationMessageType.setExpression("Rules Mock Expression");
            validationMessageType.setLevel("Rules Mock Level 42");
            validationMessageType.setMessage("Rules Mock Message");
            validationMessageType.setNote("Rules Mock Note");
            validationMessageType.setBrId("Rules Mock BRId");
            validationMessageType.setErrorType(ErrorType.WARNING);
            response.getValidationsListResponse().add(validationMessageType);*/

            String stringResponse = "";//JAXBMarshaller.marshallJaxBObjectToString(response);
            messageProducer.sendResponseMessageToSender((TextMessage) message, stringResponse);


        } catch (Exception e) {
        }
    }

}
