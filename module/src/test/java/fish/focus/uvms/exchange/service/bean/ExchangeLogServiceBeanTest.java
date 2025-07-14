package fish.focus.uvms.exchange.service.bean;

import fish.focus.schema.exchange.v1.ExchangeLogStatusTypeType;
import fish.focus.uvms.exchange.service.entity.exchangelog.ExchangeLog;
import fish.focus.uvms.exchange.service.entity.exchangelog.ExchangeLogStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeLogServiceBeanTest {

    @InjectMocks
    private ExchangeLogServiceBean exchangeLogService;

    @Mock
    private ExchangeLogModelBean exchangeLogModel;

    @Test
    public void updateStatusByLogGuidWhenSuccess() {
        //data set
        ArgumentCaptor<ExchangeLogStatus> captorForExchangeLogStatus = ArgumentCaptor.forClass(ExchangeLogStatus.class);
        ExchangeLog expectedUpdatedLog = new ExchangeLog();
        UUID logGuid = UUID.randomUUID();
        expectedUpdatedLog.setId(logGuid);
        ExchangeLogStatusTypeType status = ExchangeLogStatusTypeType.SUCCESSFUL;

        //mock
        doReturn(expectedUpdatedLog).when(exchangeLogModel).updateExchangeLogStatus(isA(ExchangeLogStatus.class), eq("SYSTEM"), isA(UUID.class));

        //execute
        ExchangeLog actualUpdatedLog = exchangeLogService.updateStatus(logGuid.toString(), status, "SYSTEM");

        //verify and assert
        verify(exchangeLogModel).updateExchangeLogStatus(captorForExchangeLogStatus.capture(), eq("SYSTEM"), isA(UUID.class));

        assertSame(expectedUpdatedLog, actualUpdatedLog);

        ExchangeLogStatus capturedExchangeLogStatus = captorForExchangeLogStatus.getValue();
        assertEquals("SYSTEM", capturedExchangeLogStatus.getUpdatedBy());
        assertEquals(ExchangeLogStatusTypeType.SUCCESSFUL, capturedExchangeLogStatus.getStatus());
    }

    @Test
    public void updateStatusByLogGuidWhenFailure() {
        String id = UUID.randomUUID().toString();
        String expectedExceptionMessage = "noooooooooooooooooooo!!!";

        //mock
        doThrow(new RuntimeException(expectedExceptionMessage))
                .when(exchangeLogModel)
                .updateExchangeLogStatus(isA(ExchangeLogStatus.class), eq("SYSTEM"), isA(UUID.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> exchangeLogService.updateStatus(id, ExchangeLogStatusTypeType.FAILED, "SYSTEM"));
        assertThat(exception.getMessage(), is(equalTo(expectedExceptionMessage)));
    }
}
