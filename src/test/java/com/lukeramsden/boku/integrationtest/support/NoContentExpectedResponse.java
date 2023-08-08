package com.lukeramsden.boku.integrationtest.support;

import org.assertj.core.api.SoftAssertions;

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
    public Map<String, List<String>> expectedHeaders()
    {
        return Map.of();
    }

    @Override
    public void assertResponseBodyMatchesExpectedBody(SoftAssertions softAssertions, String responseBody)
    {
        softAssertions.assertThat(responseBody).isEmpty();
    }
}
