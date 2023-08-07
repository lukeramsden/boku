package com.lukeramsden.boku.integrationtest.support;

public record EmptyBodyRequestToSend(
        String method,
        String path
) implements IntegrationDsl.IntegrationDslApi.RequestToSend
{
    @Override
    public byte[] body()
    {
        return new byte[0];
    }

    @Override
    public String contentType()
    {
        return null;
    }
}
