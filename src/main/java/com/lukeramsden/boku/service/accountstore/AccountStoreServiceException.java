package com.lukeramsden.boku.service.accountstore;

public sealed abstract class AccountStoreServiceException
        extends Exception
        permits AccountStoreServiceException.BalanceCannotBeBelowZeroException,
        AccountStoreServiceException.InsufficientBalanceException
{
    public static final class BalanceCannotBeBelowZeroException extends AccountStoreServiceException
    {

    }

    public static final class InsufficientBalanceException extends AccountStoreServiceException
    {

    }
}
