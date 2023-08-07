package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import com.lukeramsden.boku.integrationtest.support.JsonExpectedResponse;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class UserAccountTransferRequestValidationIntegrationTest
{
    static final String USER_1 = "user1";
    static final String USER_2 = "user2";

    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    UserAccountFixtures fixtures = new UserAccountFixtures();

    @BeforeEach
    void setInitialUserBalances()
    {
        dsl.given().sendsRequest(fixtures.admin().setsUserBalanceTo(USER_1, 100));
        dsl.and().receivesResponse(fixtures.admin().expectedUserBalanceSetSuccessfullyResponse());
        dsl.and().sendsRequest(fixtures.admin().setsUserBalanceTo(USER_2, 200));
        dsl.and().receivesResponse(fixtures.admin().expectedUserBalanceSetSuccessfullyResponse());
    }

    @Test
    void shouldRejectMissingFrom()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).invalidTransferMissingFrom(USER_2));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Missing field 'from'")));
    }

    @Test
    void shouldRejectMissingTo()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).invalidTransferMissingTo());
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Missing field 'to'")));
    }

    @Test
    void shouldRejectAmountBelowZero()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).invalidTransferAmountBelowZero(USER_2));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Cannot set field 'amount' to a value below zero")));
    }

    @Test
    void shouldRejectAmountNaN()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).invalidTransferAmountNaN(USER_2));
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", "Could not parse field 'amount' as a number")));
    }
}
