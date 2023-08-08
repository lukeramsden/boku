package com.lukeramsden.boku.integrationtest.support;

import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;

public record PlainTextExpectedResponse(
        int statusCode,
        String expectedBody
) implements IntegrationDsl.IntegrationDslApi.ExpectedResponse
{
    @Override
    public int expectedStatusCode()
    {
        return statusCode;
    }

    @Override
    public Map<String, List<String>> expectedHeaders()
    {
        return Map.of("content-type", List.of("text/plain"));
    }

    @Override
    public void assertResponseBodyMatchesExpectedBody(SoftAssertions softAssertions, String responseBody)
    {
        softAssertions.assertThat(responseBody).isEqualTo(expectedBody);
    }
}
