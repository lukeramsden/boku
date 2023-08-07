package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;

class HttpApiService
{
    private final WithdrawalService withdrawalService;
    private final AccountStoreService accountStoreService;

    public HttpApiService(WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        this.withdrawalService = withdrawalService;
        this.accountStoreService = accountStoreService;
    }

    public void shutdown()
    {

    }
}
