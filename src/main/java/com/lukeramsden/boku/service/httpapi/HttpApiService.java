package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

class HttpApiService extends AbstractVerticle
{
    private final HttpApiServiceVerticle verticle;

    public HttpApiService(Vertx vertx, WithdrawalService withdrawalService, AccountStoreService accountStoreService)
    {
        this.verticle = new HttpApiServiceVerticle(accountStoreService, withdrawalService);

        deployVerticle(vertx);
    }

    private void deployVerticle(Vertx vertx)
    {
        try
        {
            vertx.deployVerticle(verticle).toCompletionStage().toCompletableFuture().get(30, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException(e);
        }
    }
}
