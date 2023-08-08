package com.lukeramsden.boku.service.accountstore;

import com.lukeramsden.boku.infrastructure.clock.EpochClock;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.EpochNanoClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountStoreServiceStubLifecycle implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AccountStoreServiceStub accountStoreServiceStub;
    private final AgentRunner agentRunner;

    public AccountStoreServiceStubLifecycle(EpochClock clock, WithdrawalService withdrawalService)
    {
        this.accountStoreServiceStub = new AccountStoreServiceStub(clock, withdrawalService);
        this.agentRunner = new AgentRunner(
                new BackoffIdleStrategy(),
                throwable -> LOGGER.error("Error in account store service stub agent", throwable),
                null,
                accountStoreServiceStub
        );
        AgentRunner.startOnThread(agentRunner);
    }

    public static AccountStoreServiceStubLifecycle launch(EpochClock clock, WithdrawalService withdrawalService)
    {
        return new AccountStoreServiceStubLifecycle(clock, withdrawalService);
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
