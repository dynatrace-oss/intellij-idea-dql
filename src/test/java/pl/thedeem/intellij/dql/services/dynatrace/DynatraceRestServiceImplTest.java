package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.SupervisorKt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.DynatraceRestClient;
import pl.thedeem.intellij.common.sdk.model.*;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DynatraceRestServiceImplTest extends LightPlatformCodeInsightFixture4TestCase {
    private DynatraceRestClient clientMock;
    private Job testScopeJob;
    private DynatraceRestServiceImpl service;
    private final DynatraceTenant tenant = new DynatraceTenant("test-tenant", "https://test.example.com", "test-cred-id");

    @Before
    public void setup() throws Exception {
        clientMock = mock(DynatraceRestClient.class);
        DynatraceTenantsService tenantsServiceMock = mock(DynatraceTenantsService.class);
        testScopeJob = SupervisorKt.SupervisorJob(null);
        CoroutineScope testScope = CoroutineScopeKt.CoroutineScope(testScopeJob);
        ServiceContainerUtil.registerOrReplaceServiceInstance(
            ApplicationManager.getApplication(),
            DynatraceTenantsService.class,
            tenantsServiceMock,
            getTestRootDisposable()
        );
        when(tenantsServiceMock.resolveApiToken(getProject(), tenant)).thenReturn("test-token");
        service = new DynatraceRestServiceImpl(getProject(), testScope, clientMock, 0L);
    }

    @After
    public void teardownScope() {
        testScopeJob.cancel(null);
    }
    
    @Test
    public void shouldReturnVerifyResponseOnSuccessfulVerification() throws Exception {
        DQLVerifyResponse verifyResponse = new DQLVerifyResponse();
        verifyResponse.valid = true;
        when(clientMock.verifyDQL(anyString(), any(DQLVerifyPayload.class), eq("test-token")))
            .thenReturn(verifyResponse);

        DQLVerifyResponse result = service.verifyQuery(tenant, "fetch logs").get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.valid);
        verify(clientMock).verifyDQL(anyString(), any(DQLVerifyPayload.class), eq("test-token"));
    }

    @Test
    public void shouldPropagateExceptionDuringVerifyQuery() throws Exception {
        when(clientMock.verifyDQL(anyString(), any(DQLVerifyPayload.class), any()))
            .thenThrow(new RuntimeException("connection failed"));

        try {
            service.verifyQuery(tenant, "fetch logs").get(5, TimeUnit.SECONDS);
            fail("Expected ExecutionException was not thrown");
        } catch (ExecutionException e) {
            assertInstanceOf(e.getCause(), RuntimeException.class);
            assertEquals("connection failed", e.getCause().getMessage());
        }
    }

    @Test
    public void shouldReturnAutocompleteResultOnSuccess() throws Exception {
        DQLAutocompleteResult autocompleteResult = new DQLAutocompleteResult();
        when(clientMock.autocomplete(anyString(), any(DQLAutocompletePayload.class), eq("test-token")))
            .thenReturn(autocompleteResult);

        DQLAutocompleteResult result = service.autocompleteQuery(tenant, "fetch logs", 5L).get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        verify(clientMock).autocomplete(anyString(), any(DQLAutocompletePayload.class), eq("test-token"));
    }

    @Test
    public void shouldPropagateExceptionDuringAutocompleteQuery() throws Exception {
        when(clientMock.autocomplete(anyString(), any(DQLAutocompletePayload.class), any()))
            .thenThrow(new RuntimeException("network error"));

        try {
            service.autocompleteQuery(tenant, "fetch logs", 0L).get(5, TimeUnit.SECONDS);
            fail("Expected ExecutionException was not thrown");
        } catch (ExecutionException e) {
            assertInstanceOf(e.getCause(), RuntimeException.class);
            assertEquals("network error", e.getCause().getMessage());
        }
    }

    @Test
    public void shouldReturnPollResponseWhenQueryFinishesOnFirstPoll() throws Exception {
        stubQueryExecution("token-123", pollResponse("SUCCEEDED", 0L));

        DQLPollResponse result = service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), null)
            .get(10, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals("SUCCEEDED", result.getState());
        verify(clientMock).executeDQL(anyString(), any(DQLExecutePayload.class), eq("test-token"));
        verify(clientMock).pollDQLState(anyString(), eq("token-123"), eq("test-token"));
    }

    @Test
    public void shouldPollMultipleTimesUntilQueryReachesTerminalState() throws Exception {
        DQLPollResponse running = pollResponse("RUNNING", 50L);
        stubQueryExecution("token-456", running, running, pollResponse("SUCCEEDED", 100L));

        DQLPollResponse result = service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), null)
            .get(10, TimeUnit.SECONDS);

        assertEquals("SUCCEEDED", result.getState());
        verify(clientMock, times(3)).pollDQLState(anyString(), eq("token-456"), eq("test-token"));
    }

    @Test
    public void shouldInvokeProgressConsumerOnEachPollUpdate() throws Exception {
        stubQueryExecution("token-789", pollResponse("RUNNING", 50L), pollResponse("SUCCEEDED", 100L));

        List<DQLPollResponse> receivedResponses = new ArrayList<>();
        CompletableFuture<DQLPollResponse> future = service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), receivedResponses::add);
        PlatformTestUtil.waitWithEventsDispatching("executeQuery did not complete in time", future::isDone, 10);

        assertEquals(2, receivedResponses.size());
        assertEquals("RUNNING", receivedResponses.get(0).getState());
        assertEquals("SUCCEEDED", receivedResponses.get(1).getState());
    }

    @Test
    public void shouldCompleteExceptionallyWhenExecuteResponseHasNoRequestToken() throws Exception {
        stubExecuteDQL(null);

        try {
            service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), null).get(10, TimeUnit.SECONDS);
            fail("Expected ExecutionException was not thrown");
        } catch (ExecutionException e) {
            assertInstanceOf(e.getCause(), IllegalStateException.class);
        }
    }

    @Test
    public void shouldPropagateExceptionDuringExecuteQuery() throws Exception {
        when(clientMock.executeDQL(anyString(), any(DQLExecutePayload.class), any()))
            .thenThrow(new RuntimeException("server error"));

        try {
            service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), null).get(10, TimeUnit.SECONDS);
            fail("Expected ExecutionException was not thrown");
        } catch (ExecutionException e) {
            assertInstanceOf(e.getCause(), RuntimeException.class);
            assertEquals("server error", e.getCause().getMessage());
        }
    }

    @Test
    public void shouldSendCancelRequestToDynatraceWhenFutureIsCancelled() throws Exception {
        stubQueryExecution("token-cancel", pollResponse("RUNNING", 0L));
        when(clientMock.cancelDQL(anyString(), eq("token-cancel"), eq("test-token")))
            .thenReturn(pollResponse("CANCELLED", 0L));

        var future = service.executeQuery(tenant, new DQLExecutePayload("fetch logs"), null);
        PlatformTestUtil.waitWithEventsDispatching(
                "Waiting for initial pollDQLState call before cancellation",
                () -> {
                    try {
                        verify(clientMock, atLeastOnce()).pollDQLState(anyString(), eq("token-cancel"), eq("test-token"));
                        return true;
                    } catch (Throwable ignored) {
                        return false;
                    }
                },
                10
        );
        future.cancel(true);
        PlatformTestUtil.waitWithEventsDispatching(
                "Waiting for Dynatrace DQL cancel request",
                () -> {
                    try {
                        verify(clientMock).cancelDQL(anyString(), eq("token-cancel"), eq("test-token"));
                        return true;
                    } catch (Throwable ignored) {
                        return false;
                    }
                },
                10
        );

        verify(clientMock, atLeastOnce()).pollDQLState(anyString(), eq("token-cancel"), eq("test-token"));
        verify(clientMock).cancelDQL(anyString(), eq("token-cancel"), eq("test-token"));
    }

    // endregion

    private static DQLPollResponse pollResponse(String state, long progress) {
        DQLPollResponse response = new DQLPollResponse();
        response.state = state;
        response.progress = progress;
        return response;
    }

    private void stubExecuteDQL(@Nullable String requestToken) throws Exception {
        DQLExecuteResponse executeResponse = mock(DQLExecuteResponse.class);
        when(executeResponse.getRequestToken()).thenReturn(requestToken);
        when(clientMock.executeDQL(anyString(), any(DQLExecutePayload.class), eq("test-token")))
            .thenReturn(executeResponse);
    }

    private void stubPollDQLState(String requestToken, DQLPollResponse first, DQLPollResponse... more) throws Exception {
        when(clientMock.pollDQLState(anyString(), eq(requestToken), eq("test-token")))
            .thenReturn(first, more);
    }

    private void stubQueryExecution(String requestToken, DQLPollResponse first, DQLPollResponse... more) throws Exception {
        stubExecuteDQL(requestToken);
        stubPollDQLState(requestToken, first, more);
    }
}
