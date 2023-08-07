package com.lukeramsden.boku.service.withdrawal;

import java.io.Closeable;

public final class WithdrawalServiceStubLifecycle implements AutoCloseable
{
    private final WithdrawalServiceStub withdrawalServiceStub;

    private WithdrawalServiceStubLifecycle()
    {
        this.withdrawalServiceStub = new WithdrawalServiceStub();
    }

    public static WithdrawalServiceStubLifecycle launch()
    {
        return new WithdrawalServiceStubLifecycle();
    }

    @Override
    public void close()
    {

    }

    public WithdrawalServiceStub service()
    {
        return withdrawalServiceStub;
    }
}
