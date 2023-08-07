package com.lukeramsden.boku.integrationtest.support;

import java.util.List;
import java.util.Map;

public class NoContentExpectedResponse implements IntegrationDsl.IntegrationDslApi.ExpectedResponse
{
    @Override
    public int expectedStatusCode()
    {
        return 204;
    }

    @Override
    public String expectedBody()
    {
        return null;
    }

    @Override
    public Map<String, List<String>> expectedHeaders()
    {
        return Map.of();
    }
}
