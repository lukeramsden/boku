package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceException;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
                            errResponse(context, 404, "User not found");
                        });
                    })
                    .onFailure(err ->
                    {
                        errResponse(context, 500, "Error while retrieving user balance");
                    });
        }).failureHandler(this::internalServerError);
    }

    private void setUserBalanceAsAdminRoute(Router router)
    {
        router.post("/admin/setUserBalance").consumes("application/json")
                .handler(context ->
                {
                    context.request().bodyHandler(bodyHandler ->
                    {
                        final JsonObject body = bodyHandler.toJsonObject();

                        if (!body.containsKey("username"))
                        {
                            errResponse(context, 400, "Missing field 'username'");
                            return;
                        }

                        if (!body.containsKey("balance"))
                        {
                            errResponse(context, 400, "Missing field 'balance'");
                            return;
                        }

                        final BigDecimal balance;
                        try
                        {
                            balance = new BigDecimal(body.getString("balance"));
                        } catch (NumberFormatException e)
                        {
                            errResponse(context, 400, "Could not parse field 'balance' as a number");
                            return;
                        }

                        accountStoreService
                                .adminSetUserBalance(body.getString("username"), balance)
                                .onSuccess(__ ->
                                {
                                    context.response().setStatusCode(204).end();
                                })
                                .onFailure(err ->
                                {
                                    if (err instanceof AccountStoreServiceException.BalanceCannotBeBelowZeroException)
                                    {

                                        errResponse(context, 400, "Cannot set field 'balance' to a value below zero");
                                        return;
                                    }

                                    errResponse(context, 500, "Error while setting user balance");
                                });
                    });
                }).failureHandler(this::internalServerError);
    }

    private static void errResponse(RoutingContext context, final int statusCode, final String errMsg)
    {
        context.response().setStatusCode(statusCode);
        context.json(errJson(errMsg));
    }

    private static JsonObject errJson(final String err)
    {
        return new JsonObject().put("err", err);
    }

    private void internalServerError(RoutingContext context)
    {
        context.response().setStatusCode(500).end();
    }
}