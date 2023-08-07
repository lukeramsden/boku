package com.lukeramsden.boku.integrationtest.support;

import com.lukeramsden.boku.infrastructure.vertx.VertxLifecycle;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceStubLifecycle;
import com.lukeramsden.boku.service.httpapi.HttpApiServiceLifecycle;
import com.lukeramsden.boku.service.withdrawal.WithdrawalServiceStubLifecycle;
import org.agrona.CloseHelper;
import org.agrona.LangUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public final class IntegrationDsl implements BeforeEachCallback, AfterEachCallback
{
    private final IntegrationDslBehaviour behaviour;

    private IntegrationDsl()
    {
        this.behaviour = new IntegrationDslBehaviour();
    }

    public static IntegrationDsl newDsl()
    {
        return new IntegrationDsl();
    }

    @Override
    public void beforeEach(ExtensionContext context)
    {
        behaviour.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        behaviour.afterEach(context);
    }

    public IntegrationDslApi given()
    {
        return behaviour;
    }

    public IntegrationDslApi when()
    {
        return behaviour;
    }

    public IntegrationDslApi then()
    {
        return behaviour;
    }

    public IntegrationDslApi and()
    {
        return behaviour;
    }

    public interface IntegrationDslApi
    {
        void sendsRequest(RequestToSend requestToSend);

        void receivesResponse(ExpectedResponse expectedResponse);

        interface RequestToSend
        {
        }

        interface ExpectedResponse
        {
            int expectedStatusCode();

            String expectedBody();

            Map<String, List<String>> expectedHeaders();
        }
    }

    private static final class IntegrationDslBehaviour implements IntegrationDslApi, BeforeEachCallback, AfterEachCallback
    {
        private VertxLifecycle vertx;
        private WithdrawalServiceStubLifecycle withdrawalService;
        private AccountStoreServiceStubLifecycle accountStoreService;
        private HttpApiServiceLifecycle httpApiService;
        private HttpClient httpClient;
        private List<HttpResponse<String>> responses;
        private int responseAssertionWatermark;

        @Override
        public void beforeEach(ExtensionContext context)
        {
            this.responses = new ArrayList<>();
            this.responseAssertionWatermark = 0;

            try
            {
                this.vertx = VertxLifecycle.launch();
                this.withdrawalService = WithdrawalServiceStubLifecycle.launch();
                this.accountStoreService = AccountStoreServiceStubLifecycle.launch();
                this.httpApiService = HttpApiServiceLifecycle.launch(
                        vertx.vertx(),
                        withdrawalService.service(),
                        accountStoreService.service()
                );
                this.httpClient = HttpClient.newHttpClient();
            } catch (Exception e)
            {
                LangUtil.rethrowUnchecked(e);
            }
        }

        @Override
        public void afterEach(ExtensionContext context)
        {
            // Reverse order of initialisation
            CloseHelper.closeAll(httpApiService, accountStoreService, withdrawalService, vertx);

            if (responses.size() > responseAssertionWatermark)
            {
                assertThat(false).withFailMessage("There are still responses that have not been asserted on").isTrue();
            }
        }

        @Override
        public void sendsRequest(RequestToSend requestToSend)
        {
            try
            {
                final HttpResponse<String> response = httpClient.sendAsync(
                        HttpRequest.newBuilder()
                                .method("GET", HttpRequest.BodyPublishers.noBody())
                                .uri(URI.create("http://localhost:8888/").resolve("/healthz"))
                                .timeout(Duration.ofSeconds(30))
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                ).get(30, TimeUnit.SECONDS);

                responses.add(response);
            } catch (InterruptedException | TimeoutException | ExecutionException e)
            {
                LangUtil.rethrowUnchecked(e);
            }
        }

        @Override
        public void receivesResponse(ExpectedResponse expectedResponse)
        {
            final SoftAssertions softAssertions = new SoftAssertions();

            try
            {
                final HttpResponse<String> nextResponse = responses.get(responseAssertionWatermark);

                softAssertions.assertThat(nextResponse.statusCode()).isEqualTo(expectedResponse.expectedStatusCode());
                softAssertions.assertThat(nextResponse.body()).isEqualTo(expectedResponse.expectedBody());

                final Map<String, List<String>> headers = new HashMap<>(nextResponse.headers().map());
                headers.remove(":status"); // don't assert on this header
                headers.remove("content-length"); // don't assert on this header
                softAssertions.assertThat(headers).usingRecursiveAssertion().isEqualTo(expectedResponse.expectedHeaders());

                responseAssertionWatermark++;
            } catch (final IndexOutOfBoundsException ignored)
            {
                softAssertions.assertThat(false).withFailMessage("No more responses").isTrue();
            }

            softAssertions.assertAll();
        }
    }
}
