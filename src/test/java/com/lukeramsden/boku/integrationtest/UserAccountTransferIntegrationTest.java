package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class UserAccountTransferIntegrationTest
{
    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    UserAccountFixtures fixtures = new UserAccountFixtures();

    @BeforeEach
    void setInitialUserBalances()
    {
        dsl.given().sendsRequest(fixtures.admin().setsUserBalanceTo("user1", 100));
        dsl.and().receivesResponse(fixtures.admin().expectedUserBalanceSetSuccessfullyResponse());
        dsl.and().sendsRequest(fixtures.admin().setsUserBalanceTo("user2", 200));
        dsl.and().receivesResponse(fixtures.admin().expectedUserBalanceSetSuccessfullyResponse());
    }

    @Test
    void shouldBeAbleToTransferAllMoniesFromOneUserToAnother()
    {
        dsl.when().sendsRequest(fixtures.user("user1").sendsMoneyTo("user2", 100));
        dsl.then().receivesResponse(fixtures.user("user1").expectedSuccessfulTransferResponse());

        dsl.when().sendsRequest(fixtures.user("user1").queriesBalance());
        dsl.then().receivesResponse(fixtures.user("user1").expectedBalanceQueryResponse(0));

        dsl.when().sendsRequest(fixtures.user("user2").queriesBalance());
        dsl.then().receivesResponse(fixtures.user("user2").expectedBalanceQueryResponse(300));
    }

    @Test
    void shouldNotBeAbleToSendMoreMoneyThanHasInBalance()
    {
        dsl.when().sendsRequest(fixtures.user("user1").sendsMoneyTo("user2", 101));
        dsl.then().receivesResponse(fixtures.user("user1").expectedNotEnoughMoneyInBalanceResponse());

        dsl.when().sendsRequest(fixtures.user("user1").queriesBalance());
        dsl.then().receivesResponse(fixtures.user("user1").expectedBalanceQueryResponse(100));

        dsl.when().sendsRequest(fixtures.user("user2").queriesBalance());
        dsl.then().receivesResponse(fixtures.user("user2").expectedBalanceQueryResponse(200));
    }
}
