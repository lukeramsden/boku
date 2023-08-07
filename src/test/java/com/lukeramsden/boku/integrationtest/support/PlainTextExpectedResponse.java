package com.lukeramsden.boku.integrationtest.support;

import java.util.List;
import java.util.Map;

public record PlainTextExpectedResponse(
        int statusCode,
        String body
) implements IntegrationDsl.IntegrationDslApi.ExpectedResponse
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
