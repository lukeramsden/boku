package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class UserAccountTransferIntegrationTest
{
    static final String USER_1 = "user1";
    static final String USER_2 = "user2";
    static final String NON_EXISTENT_USER = "nonExistentUser";

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
    void shouldBeAbleToTransferAllMoniesFromOneUserToAnother()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).sendsMoneyTo(USER_2, 100));
        dsl.then().receivesResponse(fixtures.user(USER_1).expectedSuccessfulTransferResponse());

        dsl.when().sendsRequest(fixtures.user(USER_1).queriesBalance());
        dsl.then().receivesResponse(fixtures.user(USER_1).expectedBalanceQueryResponse(0));

        dsl.when().sendsRequest(fixtures.user(USER_2).queriesBalance());
        dsl.then().receivesResponse(fixtures.user(USER_2).expectedBalanceQueryResponse(300));
    }

    @Test
    void shouldNotBeAbleToSendMoreMoneyThanHasInBalance()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).sendsMoneyTo(USER_2, 101));
        dsl.then().receivesResponse(fixtures.user(USER_1).expectedNotEnoughMoneyInBalanceResponse());

        dsl.when().sendsRequest(fixtures.user(USER_1).queriesBalance());
        dsl.then().receivesResponse(fixtures.user(USER_1).expectedBalanceQueryResponse(100));

        dsl.when().sendsRequest(fixtures.user(USER_2).queriesBalance());
        dsl.then().receivesResponse(fixtures.user(USER_2).expectedBalanceQueryResponse(200));
    }

    @Test
    void shouldRejectRequestWhenFromUserDoesNotExist()
    {
        dsl.when().sendsRequest(fixtures.user(NON_EXISTENT_USER).sendsMoneyTo(USER_2, 10));
        dsl.then().receivesResponse(fixtures.user(NON_EXISTENT_USER).expectedUserNotFoundResponse());
    }

    @Test
    void shouldRejectRequestWhenToUserDoesNotExist()
    {
        dsl.when().sendsRequest(fixtures.user(USER_1).sendsMoneyTo(NON_EXISTENT_USER, 10));
        dsl.then().receivesResponse(fixtures.user(NON_EXISTENT_USER).expectedUserNotFoundResponse());
    }
}
