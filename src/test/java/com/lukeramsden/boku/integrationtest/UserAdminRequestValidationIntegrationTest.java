package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import com.lukeramsden.boku.integrationtest.support.JsonExpectedResponse;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class UserAdminRequestValidationIntegrationTest
{
    static final String USER_1 = "user1";

    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    UserAccountFixtures fixtures = new UserAccountFixtures();

    @Test
    void shouldRejectUserBalanceRequestMissingUsername()
    {
        dsl.when().sendsRequest(fixtures.admin().invalidSetUserBalanceRequestMissingUsername());
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Missing field 'username'")));
    }

    @Test
    void shouldRejectUserBalanceRequestMissingBalance()
    {
        dsl.when().sendsRequest(fixtures.admin().invalidSetUserBalanceRequestMissingBalance(USER_1));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Missing field 'balance'")));
    }

    @Test
    void shouldRejectUserBalanceRequestBalanceBelowZero()
    {
        dsl.when().sendsRequest(fixtures.admin().invalidSetUserBalanceRequestBalanceBelowZero(USER_1));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Cannot set field 'balance' to a value below zero")));
    }

    @Test
    void shouldRejectUserBalanceRequestBalanceNaN()
    {
        dsl.when().sendsRequest(fixtures.admin().invalidSetUserBalanceRequestBalanceNaN(USER_1));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Could not parse field 'balance' as a number")));
    }
}
