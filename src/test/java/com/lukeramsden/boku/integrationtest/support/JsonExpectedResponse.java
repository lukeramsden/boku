package com.lukeramsden.boku.integrationtest.support;

import io.vertx.core.json.JsonObject;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;

public record JsonExpectedResponse(
        int statusCode,
        JsonObject expectedBody
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
        return Map.of("content-type", List.of("application/json"));
    }

    @Override
    public void assertResponseBodyMatchesExpectedBody(SoftAssertions softAssertions, String responseBody)
    {
        JsonObject parsedBody;

        try
        {
            parsedBody = new JsonObject(responseBody);
        } catch (Exception e)
        {
            softAssertions.assertThat(false).withFailMessage("Could not parse as JSON").isTrue();
            return;
        }

        softAssertions.assertThat(parsedBody)
                .usingRecursiveComparison()
                .isEqualTo(expectedBody);
    }
}
