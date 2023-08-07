package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.fixtures.HealthCheckTestFixtures;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HealthCheckIntegrationTest
{
    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();
    HealthCheckTestFixtures fixtures = new HealthCheckTestFixtures();

    @Test
    void shouldRespondToHealthcheck()
    {
        dsl.when().sendsRequest(fixtures.healthCheckRequest());
        dsl.then().receivesResponse(fixtures.expectedHealthCheckResponse());
    }
}
