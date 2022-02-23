package fish.focus.uvms.exchange.service.mapper;

import fish.focus.schema.exchange.movement.v1.MovementBaseType;
import fish.focus.schema.exchange.movement.v1.MovementComChannelType;
import fish.focus.uvms.exchange.service.mapper.MovementMapper;
import fish.focus.uvms.exchange.service.model.IncomingMovement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MovementMapperTest {

    @Test
    @OperateOnDeployment("exchangeservice")
    public void setLesReportTimeTest(){
    	Date today = new Date();
    	MovementBaseType movement = new MovementBaseType();
    	movement.setComChannelType(MovementComChannelType.MOBILE_TERMINAL);
   		movement.setLesReportTime(today);
   		
   		IncomingMovement incomingMovement = new IncomingMovement();
   		incomingMovement = MovementMapper.mapMovementBaseTypeToRawMovementType(movement);
   		
   		assertEquals(incomingMovement.getLesReportTime(), today.toInstant());
    }
  
}
