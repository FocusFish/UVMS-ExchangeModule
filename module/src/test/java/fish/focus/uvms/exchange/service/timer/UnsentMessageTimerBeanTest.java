package fish.focus.uvms.exchange.service.timer;

import static org.junit.Assert.assertThat;
import java.time.Instant;
import javax.inject.Inject;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.exchange.service.TransactionalTests;
import fish.focus.uvms.exchange.service.config.ParameterKey;
import fish.focus.uvms.exchange.service.dao.UnsentMessageDaoBean;
import fish.focus.uvms.exchange.service.entity.unsent.UnsentMessage;
import fish.focus.uvms.exchange.service.timer.UnsentMessageTimerBean;

@RunWith(Arquillian.class)
public class UnsentMessageTimerBeanTest extends TransactionalTests {

    @Inject
    UnsentMessageDaoBean unsentMessageDao;

    @Inject
    UnsentMessageTimerBean unsentMessageTimer;

    @Inject
    ParameterService parameterService;

    @Before
    public void clearAll() {
        unsentMessageDao.getAll().stream().forEach(unsentMessageDao::remove);
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void unsentMessageTimerTest() throws ConfigServiceException {
        parameterService.setStringValue(ParameterKey.UNSENT_MESSAGE_THRESHOLD.getKey(), "-1", "workaround");

        UnsentMessage unsentMessage = new UnsentMessage();
        unsentMessage.setDateReceived(Instant.now());
        unsentMessage.setUpdatedBy("test");
        unsentMessage.setMessage("Message");
        unsentMessage.setAcknowledged(true);
        unsentMessageDao.create(unsentMessage);
        int before = unsentMessageDao.getAll().size();

        unsentMessageTimer.resendUnsentMessages();

        int after = unsentMessageDao.getAll().size();
        assertThat(after, CoreMatchers.is(before - 1));
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void unsentMessageTimerNotAcknowledgedTest() throws ConfigServiceException {
        parameterService.setStringValue(ParameterKey.UNSENT_MESSAGE_THRESHOLD.getKey(), "-1", "workaround");

        UnsentMessage unsentMessage = new UnsentMessage();
        unsentMessage.setDateReceived(Instant.now());
        unsentMessage.setUpdatedBy("test");
        unsentMessage.setMessage("Message");
        unsentMessage.setAcknowledged(false);
        unsentMessageDao.create(unsentMessage);
        int before = unsentMessageDao.getAll().size();

        unsentMessageTimer.resendUnsentMessages();

        int after = unsentMessageDao.getAll().size();
        assertThat(after, CoreMatchers.is(before));
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void unsentMessageTimerNotAcknowledgeUpdatedTest() throws ConfigServiceException {
        parameterService.setStringValue(ParameterKey.UNSENT_MESSAGE_THRESHOLD.getKey(), "-1", "workaround");

        UnsentMessage unsentMessage = new UnsentMessage();
        unsentMessage.setDateReceived(Instant.now());
        unsentMessage.setUpdatedBy("test");
        unsentMessage.setMessage("Message");
        unsentMessage.setAcknowledged(false);
        unsentMessageDao.create(unsentMessage);
        int before = unsentMessageDao.getAll().size();

        unsentMessageTimer.resendUnsentMessages();

        int after = unsentMessageDao.getAll().size();
        assertThat(after, CoreMatchers.is(before));

        unsentMessage.setAcknowledged(true);
        unsentMessageTimer.resendUnsentMessages();

        int afterAcknowledged = unsentMessageDao.getAll().size();
        assertThat(afterAcknowledged, CoreMatchers.is(before - 1));
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void unsentMessageTimerDisabledTest() throws ConfigServiceException {
        parameterService.setStringValue(ParameterKey.UNSENT_MESSAGE_THRESHOLD.getKey(), "0", "test");

        UnsentMessage unsentMessage = new UnsentMessage();
        unsentMessage.setDateReceived(Instant.now());
        unsentMessage.setUpdatedBy("test");
        unsentMessage.setMessage("Message");
        unsentMessage.setAcknowledged(true);
        unsentMessageDao.create(unsentMessage);
        int before = unsentMessageDao.getAll().size();

        unsentMessageTimer.resendUnsentMessages();

        int after = unsentMessageDao.getAll().size();
        assertThat(after, CoreMatchers.is(before));
    }
}
