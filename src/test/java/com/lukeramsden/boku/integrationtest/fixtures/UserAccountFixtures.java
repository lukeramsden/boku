package com.lukeramsden.boku.integrationtest.fixtures;

import com.lukeramsden.boku.integrationtest.support.*;
import io.vertx.core.json.JsonObject;
import org.assertj.core.api.SoftAssertions;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAccountFixtures
{
    public UserAccountAdminFixtures admin()
    {
        return new UserAccountAdminFixtures();
    }

    public UserAccountUserFixtures user(String username)
    {
        return new UserAccountUserFixtures(username);
    }

    public static class UserAccountAdminFixtures
    {
        public IntegrationDsl.IntegrationDslApi.RequestToSend setsUserBalanceTo(String username, int balanceToSetTo)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/admin/setUserBalance",
                    new JsonObject()
                            .put("username", username)
                            .put("balance", String.valueOf(balanceToSetTo))
            );
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedUserBalanceSetSuccessfullyResponse()
        {
            return new NoContentExpectedResponse();
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidSetUserBalanceRequestMissingUsername()
        {
            return new JsonRequestToSend(
                    "POST",
                    "/admin/setUserBalance",
                    new JsonObject()
                            .put("balance", String.valueOf(100))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidSetUserBalanceRequestMissingBalance(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/admin/setUserBalance",
                    new JsonObject()
                            .put("username", userThatExists)
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidSetUserBalanceRequestBalanceBelowZero(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/admin/setUserBalance",
                    new JsonObject()
                            .put("username", userThatExists)
                            .put("balance", String.valueOf(-1))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidSetUserBalanceRequestBalanceNaN(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/admin/setUserBalance",
                    new JsonObject()
                            .put("username", userThatExists)
                            .put("balance", "abc")
            );
        }
    }

    public static class UserAccountUserFixtures
    {
        private final String username;

        public UserAccountUserFixtures(String username)
        {
            this.username = username;
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend queriesBalance()
        {
            return new EmptyBodyRequestToSend("GET", "/user/%s/balance".formatted(username));
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedBalanceQueryResponse(int expectedBalance)
        {
            return new JsonExpectedResponse(
                    200,
                    new JsonObject()
                            .put("balance", String.valueOf(expectedBalance))
            );
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedUserNotFoundResponse()
        {
            return new JsonExpectedResponse(
                    404,
                    new JsonObject()
                            .put("err", "User not found: '%s'".formatted(username))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend sendsMoneyTo(String usernameToSendTo, int amountToSend)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/transfer",
                    new JsonObject()
                            .put("from", username)
                            .put("to", usernameToSendTo)
                            .put("amount", String.valueOf(amountToSend))
            );
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedSuccessfulTransferResponse()
        {
            return new NoContentExpectedResponse();
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedNotEnoughMoneyInBalanceResponse()
        {
            return new JsonExpectedResponse(
                    422,
                    new JsonObject()
                            .put("err", "User does not have sufficient balance for this transfer")
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidTransferMissingFrom(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/transfer",
                    new JsonObject()
                            .put("to", userThatExists)
                            .put("amount", String.valueOf(10))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidTransferMissingTo()
        {
            return new JsonRequestToSend(
                    "POST",
                    "/transfer",
                    new JsonObject()
                            .put("from", username)
                            .put("amount", String.valueOf(10))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidTransferAmountBelowZero(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/transfer",
                    new JsonObject()
                            .put("from", username)
                            .put("to", userThatExists)
                            .put("amount", String.valueOf(-1))
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend invalidTransferAmountNaN(final String userThatExists)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/transfer",
                    new JsonObject()
                            .put("from", username)
                            .put("to", userThatExists)
                            .put("amount", "abc")
            );
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend withdrawsTo(String withdrawalId, String withdrawalAddress, int amountToWithdraw)
        {
            return new JsonRequestToSend(
                    "POST",
                    "/initiateWithdrawal",
                    new JsonObject()
                            .put("from", username)
                            .put("withdrawalId", withdrawalId)
                            .put("toAddress", withdrawalAddress)
                            .put("amount", String.valueOf(amountToWithdraw))
            );
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedSuccessfulWithdrawalInitiatedResponse(String withdrawalId)
        {
            return new NoContentExpectedResponse();
        }

        public IntegrationDsl.IntegrationDslApi.RequestToSend checksWithdrawalStatus(String withdrawalId)
        {
            return new EmptyBodyRequestToSend(
                    "GET",
                    "/withdrawalStatus/%s".formatted(withdrawalId)
            );
        }

        public IntegrationDsl.IntegrationDslApi.ExpectedResponse expectedWithdrawalCompletedOrFailedResponse(String withdrawalId)
        {
            return new IntegrationDsl.IntegrationDslApi.ExpectedResponse()
            {
                @Override
                public int expectedStatusCode()
                {
                    return 200;
                }

                @Override
                public Map<String, List<String>> expectedHeaders()
                {
                    return Map.of("content-type", List.of("application/json"));
                }

                @Override
                public void assertResponseBodyMatchesExpectedBody(SoftAssertions softAssertions, String responseBody)
                {
                    JsonObject parsedBody;

                    try
                    {
                        parsedBody = new JsonObject(responseBody);
                    } catch (Exception e)
                    {
                        softAssertions.assertThat(false).withFailMessage("Could not parse as JSON").isTrue();
                        return;
                    }

                    softAssertions.assertThat(parsedBody.fieldNames()).isEqualTo(Set.of("withdrawalId", "status"));
                    softAssertions.assertThat(parsedBody.getString("withdrawalId")).isEqualTo(withdrawalId);
                    softAssertions.assertThat(parsedBody.getString("status")).isIn("completed", "failed");
                }
            };
        }

        public Consumer<HttpResponse<String>> matchOnWithdrawalCompletionStatus(String withdrawalId, Runnable onComplete, Runnable onFailed)
        {
            return stringHttpResponse ->
            {
                assertThat(stringHttpResponse.statusCode()).isEqualTo(200);
                assertThat(stringHttpResponse.headers().map()).isEqualTo(Map.of("content-type", List.of("application/json")));
                JsonObject parsedBody = new JsonObject(stringHttpResponse.body());
                assertThat(parsedBody.getString("withdrawalId")).isEqualTo(withdrawalId);
                if (parsedBody.getString("status").equals("completed"))
                {
                    onComplete.run();
                } else if (parsedBody.getString("status").equals("failed"))
                {
                    onFailed.run();
                } else
                {
                    throw new IllegalStateException();
                }
            };
        }
    }

}
