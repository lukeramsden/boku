package com.lukeramsden.boku;

import com.lukeramsden.boku.infrastructure.clock.EpochClock;
import com.lukeramsden.boku.infrastructure.clock.SystemEpochClock;
import com.lukeramsden.boku.infrastructure.vertx.VertxLifecycle;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceStubLifecycle;
import com.lukeramsden.boku.service.httpapi.HttpApiServiceLifecycle;
import com.lukeramsden.boku.service.withdrawal.WithdrawalServiceStubLifecycle;
import org.agrona.concurrent.EpochNanoClock;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SystemEpochNanoClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServiceContainer
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args)
    {
        final EpochClock clock = new SystemEpochClock();

        try (
                var vertx = VertxLifecycle.launch();
                var withdrawalService = WithdrawalServiceStubLifecycle.launch();
                var accountStoreService = AccountStoreServiceStubLifecycle.launch(
                        clock,
                        withdrawalService.service()
                );
                var httpApiService = HttpApiServiceLifecycle.launch(
                        vertx.vertx(),
                        accountStoreService.service()
                )
        )
        {
            LOGGER.info("Service started");
            new ShutdownSignalBarrier().await();
            LOGGER.info("Service shutting down...");
        } catch (Exception e)
        {
            LOGGER.error("Error in service container", e);
        }
    }
}
