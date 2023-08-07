package com.lukeramsden.boku.service.accountstore;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountStoreServiceStubLifecycle implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AccountStoreServiceStub accountStoreServiceStub;
    private final AgentRunner agentRunner;

    public AccountStoreServiceStubLifecycle()
    {
        this.accountStoreServiceStub = new AccountStoreServiceStub();
        this.agentRunner = new AgentRunner(
                new BackoffIdleStrategy(),
                throwable -> LOGGER.error("Error in account store service stub agent", throwable),
                null,
                accountStoreServiceStub
        );
        AgentRunner.startOnThread(agentRunner);
    }

    public static AccountStoreServiceStubLifecycle launch()
    {
        return new AccountStoreServiceStubLifecycle();
    }

    @Override
    public void close()
    {
        agentRunner.close();
    }

    public AccountStoreService service()
    {
        return accountStoreServiceStub;
    }
}
