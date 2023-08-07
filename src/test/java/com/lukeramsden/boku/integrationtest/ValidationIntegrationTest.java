package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.UserAccountFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.RequestToSend;
import com.lukeramsden.boku.integrationtest.support.JsonExpectedResponse;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ValidationIntegrationTest
{
    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectInvalidRequest(final RequestToSend requestToSend, String errMsg)
    {
        dsl.when().sendsRequest(requestToSend);
        dsl.then().receivesResponse(new JsonExpectedResponse(400, new JsonObject().put("err", errMsg)));
    }

    public static Stream<Arguments> invalidRequests()
    {
        final UserAccountFixtures userAccountFixtures = new UserAccountFixtures();
        return Stream.of(
                Arguments.arguments(userAccountFixtures.admin().invalidSetUserBalanceRequestMissingUsername(), "Missing field 'username'"),
                Arguments.arguments(userAccountFixtures.admin().invalidSetUserBalanceRequestMissingBalance(), "Missing field 'balance'"),
                Arguments.arguments(userAccountFixtures.admin().invalidSetUserBalanceRequestBalanceBelowZero(), "Cannot set field 'balance' to a value below zero"),
                Arguments.arguments(userAccountFixtures.admin().invalidSetUserBalanceRequestBalanceNaN(), "Could not parse field 'balance' as a number")
//                ,Arguments.arguments(userAccountFixtures.user("user1").invalidTransferMissingFrom(), "Missing field 'from'"),
//                Arguments.arguments(userAccountFixtures.user("user1").invalidTransferMissingTo(), "Missing field 'to'"),
//                Arguments.arguments(userAccountFixtures.user("user1").invalidTransferBalanceBelowZero(), "Cannot set field 'balance' to a value below zero"),
//                Arguments.arguments(userAccountFixtures.user("user1").invalidTransferBalanceNaN(), "Could not parse field 'balance' as a number")
        );
    }
}
