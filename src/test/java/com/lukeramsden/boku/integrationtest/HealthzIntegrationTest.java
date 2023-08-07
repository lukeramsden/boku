package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.IntegrationTestFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HealthzIntegrationTest
{
    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    IntegrationTestFixtures fixtures = new IntegrationTestFixtures();

    @Test
    void shouldRespondToHealthcheck()
    {
        dsl.when().sendsRequest(fixtures.healthCheckRequest());
        dsl.then().receivesResponse(fixtures.expectedHealthCheckResponse());
    }
}
