package com.lukeramsden.boku.service.accountstore;

import java.io.Closeable;

public class AccountStoreServiceStubLifecycle implements Closeable
{
    private final AccountStoreServiceStub accountStoreServiceStub;

    public AccountStoreServiceStubLifecycle()
    {
        this.accountStoreServiceStub = new AccountStoreServiceStub();
    }

    public static AccountStoreServiceStubLifecycle launch()
    {
        return new AccountStoreServiceStubLifecycle();
    }

    @Override
    public void close()
    {

    }

    public AccountStoreService service()
    {
        return accountStoreServiceStub;
    }
}
