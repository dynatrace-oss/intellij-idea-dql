package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.SupervisorKt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.common.sdk.SimpleOAuthClient;
import pl.thedeem.intellij.common.sdk.errors.SSONotConfiguredException;
import pl.thedeem.intellij.common.sdk.errors.SSOReAuthRequiredException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DynatraceOAuthServiceImplTest extends LightPlatformCodeInsightFixture4TestCase {
    private SimpleOAuthClient clientMock;
    private PasswordSafe passwordSafeMock;
    private Job testScopeJob;
    private DynatraceOAuthServiceImpl service;

    @Before
    public void setup() {
        clientMock = mock(SimpleOAuthClient.class);
        passwordSafeMock = mock(PasswordSafe.class);
        testScopeJob = SupervisorKt.SupervisorJob(null);
        CoroutineScope testScope = CoroutineScopeKt.CoroutineScope(testScopeJob);
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                ApplicationManager.getApplication(),
                PasswordSafe.class,
                passwordSafeMock,
                getTestRootDisposable()
        );
        service = new DynatraceOAuthServiceImpl(testScope, clientMock);
    }

    @After
    public void teardownScope() {
        testScopeJob.cancel(null);
    }

    @Test
    public void shouldCompleteFutureAfterSuccessfulOAuthFlow() throws Exception {
        when(clientMock.startAuthFlow(any(), any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(validTokenMap("access-token", "refresh-token")));

        service.signIn("https://test.example.com", "cred-1").get(5, TimeUnit.SECONDS);

        verify(clientMock).startAuthFlow(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void shouldPersistRefreshTokenAfterSuccessfulSignIn() throws Exception {
        when(clientMock.startAuthFlow(any(), any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(validTokenMap("access-token", "refresh-token")));

        service.signIn("https://test.example.com", "cred-1").get(5, TimeUnit.SECONDS);

        verify(passwordSafeMock).set(any(CredentialAttributes.class), notNull());
    }

    @Test
    public void shouldCompleteExceptionallyWhenOAuthFlowFails() {
        CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("auth server unreachable"));
        when(clientMock.startAuthFlow(any(), any(), any(), any(), any(), any()))
                .thenReturn(failedFuture);

        assertThrows(ExecutionException.class, "auth server unreachable", () ->
                service.signIn("https://test.example.com", "cred-1").get(5, TimeUnit.SECONDS));
    }

    @Test
    public void shouldFetchAndReturnAccessTokenOnFirstCall() throws Exception {
        when(passwordSafeMock.getPassword(any())).thenReturn("stored-refresh-token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenReturn(validTokenMap("access-token", "new-refresh-token"));

        String result = getResult(service.resolveToken("cred-1", "https://test.example.com"));

        assertEquals("access-token", result);
        verify(clientMock).refreshToken(any(), any(), eq("stored-refresh-token"));
    }

    @Test
    public void shouldReturnCachedTokenWithoutRefreshingOnSubsequentCalls() throws Exception {
        when(passwordSafeMock.getPassword(any())).thenReturn("stored-refresh-token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenReturn(validTokenMap("access-token", "new-refresh-token"));

        getResult(service.resolveToken("cred-1", "https://test.example.com"));
        String result = getResult(service.resolveToken("cred-1", "https://test.example.com"));

        assertEquals("access-token", result);
        verify(clientMock, times(1)).refreshToken(any(), any(), any());
    }

    @Test
    public void shouldRefreshTokenWhenCachedTokenIsAboutToExpire() throws Exception {
        Map<String, Object> soonToExpireTokens = tokenMap("old-access", "old-refresh",
                Instant.now().plusSeconds(30).toEpochMilli());
        when(passwordSafeMock.getPassword(any())).thenReturn("stored-refresh-token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenReturn(soonToExpireTokens)
                .thenReturn(validTokenMap("new-access", "new-refresh"));

        getResult(service.resolveToken("cred-1", "https://test.example.com"));
        String result = getResult(service.resolveToken("cred-1", "https://test.example.com"));

        assertEquals("new-access", result);
        verify(clientMock, times(2)).refreshToken(any(), any(), any());
    }

    @Test
    public void shouldThrowSSONotConfiguredWhenNoRefreshTokenIsStored() {
        when(passwordSafeMock.getPassword(any())).thenReturn(null);

        assertThrows(SSONotConfiguredException.class, () ->
                getResult(service.resolveToken("cred-1", "https://test.example.com")));
    }

    @Test
    public void shouldSignOutAndRethrowWhenSSOReAuthExceptionOccurs() throws Exception {
        when(passwordSafeMock.getPassword(any())).thenReturn("stored-refresh-token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenThrow(new SSOReAuthRequiredException("re-auth required"));

        assertThrows(SSOReAuthRequiredException.class, () ->
                getResult(service.resolveToken("cred-1", "https://test.example.com")));
        verify(passwordSafeMock).set(any(CredentialAttributes.class), isNull());
    }

    @Test
    public void shouldClearCacheOnSignOutSoSubsequentCallRefreshesToken() throws Exception {
        when(passwordSafeMock.getPassword(any())).thenReturn("stored-refresh-token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenReturn(validTokenMap("access-token", "refresh-token"));

        getResult(service.resolveToken("cred-1", "https://test.example.com"));
        service.signOut("cred-1").get(5, TimeUnit.SECONDS);
        getResult(service.resolveToken("cred-1", "https://test.example.com"));

        verify(clientMock, times(2)).refreshToken(any(), any(), any());
    }

    @Test
    public void shouldClearCredentialsFromPasswordSafeOnSignOut() throws Exception {
        service.signOut("cred-1").get(5, TimeUnit.SECONDS);

        verify(passwordSafeMock).set(any(CredentialAttributes.class), isNull());
    }

    @Test
    public void shouldHandleConcurrentResolutionsGracefully() throws Exception {
        when(passwordSafeMock.getPassword(any())).thenReturn("token");
        when(clientMock.refreshToken(any(), any(), any()))
                .thenAnswer(inv -> {
                    Thread.sleep(100);
                    return validTokenMap("acc", "ref");
                });

        CompletableFuture<String> f1 = service.resolveToken("c1", "url");
        CompletableFuture<String> f2 = service.resolveToken("c1", "url");

        CompletableFuture.allOf(f1, f2).get(5, TimeUnit.SECONDS);

        verify(clientMock, times(1)).refreshToken(any(), any(), any());
    }

    private static <T> T getResult(CompletableFuture<T> future) throws Exception {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(cause);
        }
    }

    private static Map<String, Object> validTokenMap(String accessToken, String refreshToken) {
        return tokenMap(accessToken, refreshToken, Instant.now().plusSeconds(3600).toEpochMilli());
    }

    private static Map<String, Object> tokenMap(String accessToken, String refreshToken, long validTo) {
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        tokens.put("valid_from", System.currentTimeMillis());
        tokens.put("valid_to", validTo);
        return tokens;
    }
}
