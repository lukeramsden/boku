package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class UserWithdrawalIntegrationTest
{
    static final String USER_1 = "user1";
    public static final String WITHDRAWAL_ID = "withdrawal1";
    public static final String WITHDRAWAL_ADDRESS = "withdrawalTargetAddress";

    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    UserAccountFixtures fixtures = new UserAccountFixtures();
    UserAccountFixtures.UserAccountUserFixtures user1 = fixtures.user(USER_1);

    @BeforeEach
    void setInitialUserBalances()
    {
        dsl.given().sendsRequest(fixtures.admin().setsUserBalanceTo(USER_1, 100));
        dsl.and().receivesResponse(fixtures.admin().expectedUserBalanceSetSuccessfullyResponse());
    }

    @Test
    void shouldBeAbleToWithdraw()
    {
        dsl.when().sendsRequest(user1.withdrawsTo(WITHDRAWAL_ID, WITHDRAWAL_ADDRESS, 100));
        dsl.then().receivesResponse(user1.expectedSuccessfulWithdrawalInitiatedResponse(WITHDRAWAL_ID));

        dsl.when().sendsRequest(user1.queriesBalance());
        // for now balance includes pending withdrawals subtracted
        // it may make sense in future
        dsl.then().receivesResponse(user1.expectedBalanceQueryResponse(0));

        await().atMost(15, SECONDS)
                .untilAsserted(() ->
                {
                    dsl.when().sendsRequest(user1.checksWithdrawalStatus(WITHDRAWAL_ID));
                    // Stub can fail randomly, so we can't assert on the withdrawal
                    // succeeding - only that it completed.
                    // I would never build a system this way as having nondeterminism
                    // like this makes it very hard to test.
                    // However, those are the requirements :)
                    dsl.then().receivesResponse(user1.expectedWithdrawalCompletedOrFailedResponse(WITHDRAWAL_ID));
                });

        dsl.given().matchesOnLastResponse(
                user1.matchOnWithdrawalCompletionStatus(
                        WITHDRAWAL_ID,
                        // completed
                        () ->
                        {
                            dsl.when().sendsRequest(user1.queriesBalance());
                            dsl.then().receivesResponse(user1.expectedBalanceQueryResponse(0));
                        },
                        // failed
                        () ->
                        {
                            dsl.when().sendsRequest(user1.queriesBalance());
                            dsl.then().receivesResponse(user1.expectedBalanceQueryResponse(100));
                        }
                )
        );
    }
}
