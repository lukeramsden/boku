package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

class HttpApiServiceVerticle extends AbstractVerticle
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AccountStoreService accountStoreService;
    private final WithdrawalService withdrawalService;

    public HttpApiServiceVerticle(AccountStoreService accountStoreService, WithdrawalService withdrawalService)
    {
        this.accountStoreService = accountStoreService;
        this.withdrawalService = withdrawalService;
    }

    @Override
    public void start()
    {
        Router router = Router.router(vertx);

        router.get("/healthz").handler(context ->
        {
            context.response().putHeader("content-type", "text/plain");
            context.response().end("healthy");
        });

        setUserBalanceAsAdminRoute(router);
        getUserBalanceRoute(router);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server ->
                        LOGGER.info(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }

    private void getUserBalanceRoute(Router router)
    {
        router.get("/user/:username/balance").handler(context ->
        {
            final String username = context.pathParam("username");

            accountStoreService
                    .getUserBalance(username)
                    .onSuccess(balance ->
                    {
                        balance.ifPresentOrElse(bigDecimal ->
                        {
                            context.response().setStatusCode(200);
                            context.json(new JsonObject().put("balance", bigDecimal.toString()));
                        }, () ->
                        {
                            // there are many arguments to be had
                            // about how to return errors for missing entities
                            // / resources
                            // that argument is out of scope of this take-home task
                            context.response().setStatusCode(404);
                            context.json(new JsonObject().put("err", "User not found"));
                        });
                    })
                    .onFailure(err ->
                    {
                        LOGGER.error("Error while retrieving user balance", err);
                        context.response().setStatusCode(500).end();
                    });
        });
    }

    private void setUserBalanceAsAdminRoute(Router router)
    {
        router.post("/admin/setUserBalance").consumes("application/json")
                .handler(context ->
                {
                    context.request().bodyHandler(bodyHandler ->
                    {
                        final JsonObject body = bodyHandler.toJsonObject();
                        final String username = body.getString("username");
                        final String strBalance = body.getString("balance");

                        if (username == null || strBalance == null)
                        {
                            context.response().setStatusCode(400).end();
                            return;
                        }

                        final BigDecimal balance;
                        try
                        {
                            balance = new BigDecimal(strBalance);
                        } catch (NumberFormatException e)
                        {
                            context.response().setStatusCode(400).end();
                            return;
                        }

                        accountStoreService
                                .adminSetUserBalance(username, balance)
                                .onSuccess(__ ->
                                {
                                    context.response().setStatusCode(204).end();
                                })
                                .onFailure(err ->
                                {
                                    LOGGER.error("Error while setting user balance", err);
                                    context.response().setStatusCode(500).end();
                                });
                    });
                });
    }
}