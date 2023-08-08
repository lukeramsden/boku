package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

import static com.lukeramsden.boku.service.httpapi.ResponseHelper.ErrorMatcherRow.matchError;
import static com.lukeramsden.boku.service.httpapi.ResponseHelper.*;

class HttpApiServiceVerticle extends AbstractVerticle
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AccountStoreService accountStoreService;

    public HttpApiServiceVerticle(AccountStoreService accountStoreService)
    {
        this.accountStoreService = accountStoreService;
    }

    @Override
    public void start()
    {
        Router router = Router.router(vertx);

        router.get("/healthz")
                .handler(this::healthzHandler);

        router.get("/user/:username/balance")
                .handler(this::getUserBalanceHandler)
                .failureHandler(internalServerError());

        router.post("/admin/setUserBalance")
                .consumes("application/json")
                .handler(context -> context.request()
                        .bodyHandler(bodyHandler -> setUserBalanceAsAdminHandler(context, bodyHandler)))
                .failureHandler(internalServerError());

        router.post("/transfer")
                .consumes("application/json")
                .handler(context -> context.request()
                        .bodyHandler(bodyHandler -> transferHandler(context, bodyHandler)))
                .failureHandler(internalServerError());

        router.post("/initiateWithdrawal")
                .consumes("application/json")
                .handler(context -> context.request()
                        .bodyHandler(bodyHandler -> initiateWithdrawalHandler(context, bodyHandler)))
                .failureHandler(internalServerError());

        router.get("/withdrawalStatus/:withdrawalId")
                .handler(this::checkWithdrawalStatusHandler)
                .failureHandler(internalServerError());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server ->
                        LOGGER.info(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }

    private void healthzHandler(RoutingContext context)
    {
        context.response().putHeader("content-type", "text/plain");
        context.response().end("healthy");
    }

    private void getUserBalanceHandler(RoutingContext context)
    {
        final String username = context.pathParam("username");

        wrapInRequestContext(accountStoreService.getUserBalance(username))
                .onSuccess(balance ->
                {
                    context.response().setStatusCode(200);
                    context.json(new JsonObject().put("balance", balance.toString()));
                })
                .onFailure(errorMatcher(
                        context, "Error while retrieving user balance",
                        matchError(
                                // there are many arguments to be had
                                // about how to return errors for missing entities
                                // / resources
                                // that argument is out of scope of this take-home task
                                AccountStoreServiceException.UserDoesNotExistException.class,
                                404, err -> "User not found: '%s'".formatted(err.username())
                        )
                ));
    }

    private void setUserBalanceAsAdminHandler(RoutingContext context, Buffer bodyHandler)
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

        wrapInRequestContext(accountStoreService.adminSetUserBalance(body.getString("username"), balance))
                .onSuccess(noContentResponse(context))
                .onFailure(errorMatcher(
                        context,
                        "Error while setting user balance",
                        matchError(
                                AccountStoreServiceException.BalanceCannotBeBelowZeroException.class,
                                400, "Cannot set field 'balance' to a value below zero"
                        )
                ));
    }

    private void transferHandler(RoutingContext context, Buffer bodyHandler)
    {
        final JsonObject body = bodyHandler.toJsonObject();

        if (!body.containsKey("from"))
        {
            errResponse(context, 400, "Missing field 'from'");
            return;
        }

        if (!body.containsKey("to"))
        {
            errResponse(context, 400, "Missing field 'to'");
            return;
        }

        if (!body.containsKey("amount"))
        {
            errResponse(context, 400, "Missing field 'amount'");
            return;
        }

        final BigDecimal amount;
        try
        {
            amount = new BigDecimal(body.getString("amount"));
        } catch (NumberFormatException e)
        {
            errResponse(context, 400, "Could not parse field 'amount' as a number");
            return;
        }

        wrapInRequestContext(
                accountStoreService.transferAmountFromTo(
                        body.getString("from"),
                        body.getString("to"),
                        amount
                )
        )
                .onSuccess(noContentResponse(context))
                .onFailure(errorMatcher(
                        context, "Error while performing transfer",
                        matchError(
                                // there are many arguments to be had
                                // about how to return errors for missing entities
                                // / resources
                                // that argument is out of scope of this take-home task
                                AccountStoreServiceException.UserDoesNotExistException.class,
                                404, err -> "User not found: '%s'".formatted(err.username())
                        ),
                        matchError(
                                AccountStoreServiceException.AmountCannotBeBelowZeroException.class,
                                400, "Cannot set field 'amount' to a value below zero"
                        ),
                        matchError(
                                AccountStoreServiceException.InsufficientBalanceException.class,
                                422, "User does not have sufficient balance for this transfer"
                        )
                ));
    }

    private void initiateWithdrawalHandler(RoutingContext context, Buffer bodyHandler)
    {
        final JsonObject body = bodyHandler.toJsonObject();

        if (!body.containsKey("from"))
        {
            errResponse(context, 400, "Missing field 'from'");
            return;
        }

        if (!body.containsKey("withdrawalId"))
        {
            errResponse(context, 400, "Missing field 'withdrawalId'");
            return;
        }

        if (!body.containsKey("toAddress"))
        {
            errResponse(context, 400, "Missing field 'toAddress'");
            return;
        }

        if (!body.containsKey("amount"))
        {
            errResponse(context, 400, "Missing field 'amount'");
            return;
        }

        final BigDecimal amount;
        try
        {
            amount = new BigDecimal(body.getString("amount"));
        } catch (NumberFormatException e)
        {
            errResponse(context, 400, "Could not parse field 'amount' as a number");
            return;
        }

        wrapInRequestContext(
                accountStoreService.initiateWithdrawalToAddress(
                        body.getString("withdrawalId"),
                        body.getString("from"),
                        body.getString("toAddress"),
                        amount
                )
        )
                .onSuccess(noContentResponse(context))
                .onFailure(errorMatcher(
                        context, "Error initiating withdrawal",
                        matchError(
                                // there are many arguments to be had
                                // about how to return errors for missing entities
                                // / resources
                                // that argument is out of scope of this take-home task
                                AccountStoreServiceException.UserDoesNotExistException.class,
                                404, err -> "User not found: '%s'".formatted(err.username())
                        ),
                        matchError(
                                AccountStoreServiceException.AmountCannotBeBelowZeroException.class,
                                400, "Cannot set field 'amount' to a value below zero"
                        ),
                        matchError(
                                AccountStoreServiceException.InsufficientBalanceException.class,
                                422, "User does not have sufficient balance for this transfer"
                        )
                ));
    }

    private void checkWithdrawalStatusHandler(RoutingContext context)
    {
        throw new UnsupportedOperationException();
    }

    // Ensures that callbacks are run on correct VertX context thread
    // for sending responses
    // To be honest - really should just instantiate this all in a single verticle for now
    // rather than communicating over queues
    private <T> Future<T> wrapInRequestContext(Future<T> futureToWrap)
    {
        return Future.future(newFuture ->
        {
            futureToWrap.onSuccess(val -> context.runOnContext(__ -> newFuture.tryComplete(val)));
            futureToWrap.onFailure(throwable -> context.runOnContext(__ -> newFuture.tryFail(throwable)));
        });
    }
}