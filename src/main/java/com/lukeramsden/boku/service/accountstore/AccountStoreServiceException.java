package com.lukeramsden.boku.service.accountstore;

public sealed abstract class AccountStoreServiceException
        extends Exception
        permits AccountStoreServiceException.UserDoesNotExistException,
        AccountStoreServiceException.BalanceCannotBeBelowZeroException,
        AccountStoreServiceException.AmountCannotBeBelowZeroException,
        AccountStoreServiceException.InsufficientBalanceException,
        AccountStoreServiceException.WithdrawalDoesNotExistException,
        AccountStoreServiceException.WithdrawalAlreadyBeingProcessedException
{
    public static final class UserDoesNotExistException extends AccountStoreServiceException
    {
        private final String username;

        public UserDoesNotExistException(String username)
        {
            super();
            this.username = username;
        }

        public String username()
        {
            return username;
        }

        @Override
        public String toString()
        {
            return "UserDoesNotExistException{" +
                    "username='" + username + '\'' +
                    '}';
        }
    }

    public static final class BalanceCannotBeBelowZeroException extends AccountStoreServiceException
    {

    }

    public static final class AmountCannotBeBelowZeroException extends AccountStoreServiceException
    {

    }

    public static final class InsufficientBalanceException extends AccountStoreServiceException
    {

    }

    public static final class WithdrawalDoesNotExistException extends AccountStoreServiceException
    {

    }

    public static final class WithdrawalAlreadyBeingProcessedException extends AccountStoreServiceException
    {


    }
}
