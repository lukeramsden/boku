package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;

import java.io.Closeable;

public final class HttpApiServiceLifecycle implements Closeable
{
    private final HttpApiService httpApiService;

    private HttpApiServiceLifecycle(WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        this.httpApiService = new HttpApiService(withdrawalService, accountStoreService);
    }

    public static HttpApiServiceLifecycle launch(WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        return new HttpApiServiceLifecycle(withdrawalService, accountStoreService);
    }

    @Override
    public void close()
    {
        httpApiService.shutdown();
    }

    public HttpApiService service()
    {
        return httpApiService;
    }
}
