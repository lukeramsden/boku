package com.lukeramsden.boku.service.accountstore;

public sealed abstract class AccountStoreServiceException
        extends Exception
        permits AccountStoreServiceException.UserDoesNotExistException,
        AccountStoreServiceException.BalanceCannotBeBelowZeroException,
        AccountStoreServiceException.InsufficientBalanceException
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

    public static final class InsufficientBalanceException extends AccountStoreServiceException
    {

    }
}
