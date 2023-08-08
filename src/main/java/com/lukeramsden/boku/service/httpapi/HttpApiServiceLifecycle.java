package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import io.vertx.core.Vertx;

public final class HttpApiServiceLifecycle implements AutoCloseable
{
    private final HttpApiService httpApiService;

    private HttpApiServiceLifecycle(Vertx vertx, AccountStoreService accountStoreService)
    {
        this.httpApiService = new HttpApiService(vertx, accountStoreService);
    }

    public static HttpApiServiceLifecycle launch(Vertx vertx, AccountStoreService accountStoreService)
    {
        return new HttpApiServiceLifecycle(vertx, accountStoreService);
    }

    @Override
    public void close()
    {

    }
}
