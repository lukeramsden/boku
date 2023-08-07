package com.lukeramsden.boku.service.accountstore;

public sealed abstract class AccountStoreServiceException
        extends Exception
        permits AccountStoreServiceException.BalanceCannotBeBelowZeroException
{
    public static final class BalanceCannotBeBelowZeroException extends AccountStoreServiceException
    {

    }
}
