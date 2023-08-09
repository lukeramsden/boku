package com.lukeramsden.boku.integrationtest;

import com.lukeramsden.boku.integrationtest.support.EmptyBodyRequestToSend;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl;
import com.lukeramsden.boku.integrationtest.support.PlainTextExpectedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HealthCheckIntegrationTest
{
    @RegisterExtension
    IntegrationDsl dsl = IntegrationDsl.newDsl();

    @Test
    void shouldRespondToHealthcheck()
    {
        dsl.when().sendsRequest(new EmptyBodyRequestToSend("GET", "/healthz"));
        dsl.then().receivesResponse(new PlainTextExpectedResponse(200, "healthy"));
    }
}
