package com.lukeramsden.boku.integrationtest.fixtures;

import com.lukeramsden.boku.integrationtest.support.EmptyBodyRequestToSend;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.ExpectedResponse;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.RequestToSend;
import com.lukeramsden.boku.integrationtest.support.PlainTextExpectedResponse;

public class HealthCheckTestFixtures
{
    public RequestToSend healthCheckRequest()
    {
        return new EmptyBodyRequestToSend("GET", "/healthz");
    }

    public ExpectedResponse expectedHealthCheckResponse()
    {
        return new PlainTextExpectedResponse(200, "healthy");
    }
}
