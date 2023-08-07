package com.lukeramsden.boku.service.accountstore;

import io.vertx.core.Future;

import java.math.BigDecimal;
import java.util.Optional;


public interface AccountStoreService
{
    Future<Void> adminSetUserBalance(String username, BigDecimal balance);

    Future<Optional<BigDecimal>> getUserBalance(String username);

    Future<Void> transferAmountFromTo(String from, String to, BigDecimal amount);
}
