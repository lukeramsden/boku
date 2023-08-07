package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.Vertx;

public final class HttpApiServiceLifecycle implements AutoCloseable
{
    private final HttpApiService httpApiService;

    private HttpApiServiceLifecycle(Vertx vertx, WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        this.httpApiService = new HttpApiService(vertx, withdrawalService, accountStoreService);
    }

    public static HttpApiServiceLifecycle launch(Vertx vertx, WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        return new HttpApiServiceLifecycle(vertx, withdrawalService, accountStoreService);
    }

    @Override
    public void close()
    {

    }
}
