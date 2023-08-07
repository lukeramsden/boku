package com.lukeramsden.boku.integrationtest.fixtures;

import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.ExpectedResponse;
import com.lukeramsden.boku.integrationtest.support.IntegrationDsl.IntegrationDslApi.RequestToSend;

import java.util.List;
import java.util.Map;

public class IntegrationTestFixtures
{
    public RequestToSend healthCheckRequest()
    {
        return null;
    }

    public ExpectedResponse expectedHealthCheckResponse()
    {
        return new PlainTextExpectedResponse(200, "healthy");
    }

    private record PlainTextExpectedResponse(int statusCode, String body) implements ExpectedResponse
    {
        @Override
        public int expectedStatusCode()
        {
            return statusCode;
        }

        @Override
        public String expectedBody()
        {
            return body;
        }

        @Override
        public Map<String, List<String>> expectedHeaders()
        {
            return Map.of("content-type", List.of("text/plain"));
        }
    }
}
