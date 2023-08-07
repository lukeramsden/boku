package com.lukeramsden.boku.integrationtest.fixtures;

import com.lukeramsden.boku.integrationtest.support.*;
import io.vertx.core.json.JsonObject;

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
                            .put("err", "User not found")
            );
        }
    }

}
