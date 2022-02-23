package fish.focus.uvms.exchange.service.timer;

import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.schema.exchange.v1.LogType;
import fish.focus.schema.exchange.v1.TypeRefType;
import fish.focus.uvms.exchange.service.TestHelper;
import fish.focus.uvms.exchange.service.TransactionalTests;
import fish.focus.uvms.exchange.service.dao.ExchangeLogDaoBean;
import fish.focus.uvms.exchange.service.entity.exchangelog.ExchangeLog;
import fish.focus.uvms.exchange.service.entity.exchangelog.ExchangeLogStatus;
import fish.focus.uvms.exchange.service.timer.ExchangePollResponseTimerBean;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ExchangePollResponseTimerBeanTest extends TransactionalTests {

    @Inject
    ExchangeLogDaoBean exchangeLogDao;

    @Inject
    ExchangePollResponseTimerBean exchangeTimerBean;

    @Test
    @OperateOnDeployment("exchangeservice")
    public void pollResponseTimerTest() {
        ExchangeLog exchangeLog = TestHelper.createBasicLog();
        exchangeLog.setTypeRefType(TypeRefType.POLL);
        exchangeLog.setTypeRefGuid(UUID.randomUUID());
        exchangeLog.setType(LogType.SEND_POLL);
        exchangeLog.setStatus(ExchangeLogStatusTypeType.PENDING);
        exchangeLog.setDateReceived(Instant.now().minus(2, ChronoUnit.HOURS));
        exchangeLog.setTypeRefMessage("poll response timer test");

        TestHelper.addLogStatusToLog(exchangeLog,ExchangeLogStatusTypeType.PENDING);
        ExchangeLogStatus exchangeLogStatus = exchangeLog.getStatusHistory().get(0);
        exchangeLogStatus.setStatusTimestamp(Instant.now());
        exchangeLog = exchangeLogDao.createLog(exchangeLog);

        exchangeTimerBean.pollResponseTimer();

        ExchangeLog updatedExchangeLog = exchangeLogDao.getExchangeLogByGuid(exchangeLog.getId());

        assertEquals(ExchangeLogStatusTypeType.FAILED, updatedExchangeLog.getStatus());
        assertTrue(updatedExchangeLog.getStatusHistory().size() == 2);
        assertTrue(updatedExchangeLog.getStatusHistory().stream().anyMatch(log -> log.getStatus().equals(ExchangeLogStatusTypeType.FAILED)));
        
    }

}
