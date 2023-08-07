package com.lukeramsden.boku.integrationtest.support;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public record JsonExpectedResponse(
        int statusCode,
        JsonObject body
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
        return body.toString();
    }

    @Override
    public Map<String, List<String>> expectedHeaders()
    {
        return Map.of("content-type", List.of("application/json"));
    }
}
