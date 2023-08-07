package com.lukeramsden.boku.service.accountstore;

import io.vertx.core.Future;

import java.math.BigDecimal;


public interface AccountStoreService
{
    Future<Void> adminSetUserBalance(String username, BigDecimal balance);
}
