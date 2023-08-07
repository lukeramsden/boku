package com.lukeramsden.boku.service.httpapi;

import com.lukeramsden.boku.service.accountstore.AccountStoreService;
import com.lukeramsden.boku.service.accountstore.AccountStoreServiceException;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

import static com.lukeramsden.boku.service.httpapi.ResponseHelper.ErrorMatcherRow.matchError;
import static com.lukeramsden.boku.service.httpapi.ResponseHelper.*;

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

        healthzRoute(router);
        setUserBalanceAsAdminRoute(router);
        getUserBalanceRoute(router);
        transferRoute(router);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server ->
                        LOGGER.info(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }

    private void healthzRoute(Router router)
    {
        router.get("/healthz").handler(context ->
        {
            context.response().putHeader("content-type", "text/plain");
            context.response().end("healthy");
        });
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
        }).failureHandler(internalServerError());
    }

    private void setUserBalanceAsAdminRoute(Router router)
    {
        router.post("/admin/setUserBalance").consumes("application/json")
                .handler(context -> context.request().bodyHandler(
                        bodyHandler ->
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
                                    .onSuccess(noContentResponse(context))
                                    .onFailure(errorMatcher(
                                            context,
                                            "Error while setting user balance",
                                            matchError(
                                                    AccountStoreServiceException.BalanceCannotBeBelowZeroException.class,
                                                    400, "Cannot set field 'balance' to a value below zero"
                                            )
                                    ));
                        })
                ).failureHandler(internalServerError());
    }

    private void transferRoute(Router router)
    {
        router.post("/transfer").consumes("application/json")
                .handler(context -> context.request().bodyHandler(
                        bodyHandler ->
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

                            accountStoreService
                                    .transferAmountFromTo(body.getString("from"), body.getString("to"), amount)
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
                        })
                ).failureHandler(internalServerError());
    }
}