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
import java.util.function.Consumer;

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

        void matchesOnLastResponse(Consumer<HttpResponse<String>> matcher);

        interface RequestToSend
        {
            String method();

            String path();

            byte[] body();

            String contentType();
        }

        interface ExpectedResponse
        {
            int expectedStatusCode();

            Map<String, List<String>> expectedHeaders();

            void assertResponseBodyMatchesExpectedBody(SoftAssertions softAssertions, String responseBody);
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
                // VertX and Stub services spin up their own threads, so we don't
                // need to worry about creating any threads here
                this.vertx = VertxLifecycle.launch();
                this.withdrawalService = WithdrawalServiceStubLifecycle.launch();
                this.accountStoreService = AccountStoreServiceStubLifecycle.launch(withdrawalService.service());
                this.httpApiService = HttpApiServiceLifecycle.launch(
                        vertx.vertx(),
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
                HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

                httpRequestBuilder.method(requestToSend.method(), HttpRequest.BodyPublishers.ofByteArray(requestToSend.body()));
                if (requestToSend.contentType() != null)
                {
                    httpRequestBuilder.header("Content-Type", requestToSend.contentType());
                }
                httpRequestBuilder.uri(
                        URI.create("http://localhost:8888/").resolve(requestToSend.path())
                );
                httpRequestBuilder.timeout(Duration.ofSeconds(30));

                final HttpResponse<String> response = httpClient.sendAsync(
                        httpRequestBuilder.build(),
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
                responseAssertionWatermark++;

                assertThatResponseIsEqual(softAssertions, nextResponse, expectedResponse);
            } catch (final IndexOutOfBoundsException ignored)
            {
                softAssertions.assertThat(false).withFailMessage("No more responses").isTrue();
            }

            softAssertions.assertAll();
        }

        @Override
        public void matchesOnLastResponse(Consumer<HttpResponse<String>> matcher)
        {
            matcher.accept(responses.get(responses.size() - 1));
        }

        private static void assertThatResponseIsEqual(SoftAssertions softAssertions, HttpResponse<String> nextResponse, ExpectedResponse expectedResponse)
        {
            softAssertions.assertThat(nextResponse.statusCode()).isEqualTo(expectedResponse.expectedStatusCode());

            final Map<String, List<String>> headers = new HashMap<>(nextResponse.headers().map());
            headers.remove(":status"); // don't assert on this header
            headers.remove("content-length"); // don't assert on this header
            softAssertions.assertThat(headers).usingRecursiveAssertion().isEqualTo(expectedResponse.expectedHeaders());

            expectedResponse.assertResponseBodyMatchesExpectedBody(softAssertions, nextResponse.body());
        }
    }
}
