package com.lukeramsden.boku.service.accountstore;

import io.vertx.core.Future;

import java.math.BigDecimal;


public interface AccountStoreService
{
    Future<Void> adminSetUserBalance(String username, BigDecimal balance);

    Future<BigDecimal> getUserBalance(String username);

    Future<Void> transferAmountFromTo(String from, String to, BigDecimal amountToTransfer);

    Future<Void> initiateWithdrawalToAddress(String withdrawalId, String from, String toAddress, BigDecimal amountToWithdraw);

    Future<WithdrawalState> checkWithdrawalStatus(String withdrawalId);

    enum WithdrawalState
    {
        PROCESSING, COMPLETED, FAILED
    }
}
