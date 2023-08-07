package com.lukeramsden.boku.integrationtest.fixtures;

import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.ExpectedResponse;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.RequestToSend;

public class IntegrationTestFixtures
{
    public RequestToSend healthCheckRequest()
    {
        return null;
    }

    public ExpectedResponse expectedHealthCheckResponse()
    {
        return new ExpectedResponse()
        {
            @Override
            public int expectedStatusCode()
            {
                return 200;
            }

            @Override
            public String expectedBody()
            {
                return "healthy";
            }
        };
    }
}
