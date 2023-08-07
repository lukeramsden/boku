package com.lukeramsden.boku.integrationtest.support;

import io.vertx.core.json.JsonObject;

public record JsonRequestToSend(
        String method,
        String path,
        JsonObject jsonObject
) implements IntegrationDsl.IntegrationDslApi.RequestToSend
{
    @Override
    public String method()
    {
        return method;
    }

    @Override
    public String path()
    {
        return path;
    }

    @Override
    public byte[] body()
    {
        return jsonObject.toBuffer().getBytes();
    }

    @Override
    public String contentType()
    {
        return "application/json";
    }
}
