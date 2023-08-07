package com.lukeramsden.boku;

import com.lukeramsden.boku.infrastructure.vertx.VertxLifecycle;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceStubLifecycle;
import com.lukeramsden.boku.service.httpapi.HttpApiServiceLifecycle;
import com.lukeramsden.boku.service.withdrawal.WithdrawalServiceStubLifecycle;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServiceContainer
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args)
    {
        try (
                var vertx = VertxLifecycle.launch();
                var withdrawalService = WithdrawalServiceStubLifecycle.launch();
                var accountStoreService = AccountStoreServiceStubLifecycle.launch();
                var httpApiService = HttpApiServiceLifecycle.launch(
                        vertx.vertx(),
                        withdrawalService.service(),
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
