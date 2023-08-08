package com.lukeramsden.boku.service.accountstore;

import com.lukeramsden.boku.infrastructure.agentservice.AgentService;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.Future;
import org.agrona.collections.Hashing;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * AccountStoreServiceStub runs as an agent with a thread-safe public API
 * that queues on to an internal array-backed concurrent queue.
 * <p>
 * This allows the business logic to be single-threaded, which is much simpler
 * (and faster) than managing synchronisation amongst business logic.
 * <p>
 * In a more advanced message-based system that crossed processes (such
 * as one built using Aeron), this would allow us to take advantage of
 * batching for even better performance.
 */
class AccountStoreServiceStub extends AgentService implements AccountStoreService
{
    private final WithdrawalService withdrawalService;

    private final Map<String, ActiveWithdrawal> activeWithdrawals;
    private final Map<String, BigDecimal> userBalances;

    public AccountStoreServiceStub(WithdrawalService withdrawalService)
    {
        this.withdrawalService = withdrawalService;
        this.userBalances = new Object2ObjectHashMap<>(
                ObjectHashSet.DEFAULT_INITIAL_CAPACITY,
                Hashing.DEFAULT_LOAD_FACTOR,
                true
        );
        this.activeWithdrawals = new Object2ObjectHashMap<>(
                ObjectHashSet.DEFAULT_INITIAL_CAPACITY,
                Hashing.DEFAULT_LOAD_FACTOR,
                true
        );
    }

    @Override
    public Future<Void> adminSetUserBalance(String username, BigDecimal balance)
    {
        return task(() ->
        {
            if (balance.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new AccountStoreServiceException.BalanceCannotBeBelowZeroException();
            }

            userBalances.put(username, new BigDecimal(balance.unscaledValue(), balance.scale()));
            return null;
        });
    }

    @Override
    public Future<BigDecimal> getUserBalance(String username)
    {
        return task(() ->
        {
            if (!userBalances.containsKey(username))
            {
                throw new AccountStoreServiceException.UserDoesNotExistException(username);
            }

            return userBalances.get(username);
        });
    }

    @Override
    public Future<Void> transferAmountFromTo(String from, String to, BigDecimal amountToTransfer)
    {
        return task(() ->
        {
            if (!userBalances.containsKey(from))
            {
                throw new AccountStoreServiceException.UserDoesNotExistException(from);
            }

            if (!userBalances.containsKey(to))
            {
                throw new AccountStoreServiceException.UserDoesNotExistException(to);
            }

            if (amountToTransfer.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new AccountStoreServiceException.AmountCannotBeBelowZeroException();
            }

            final BigDecimal fromBalance = userBalances.get(from);
            final BigDecimal toBalance = userBalances.get(to);

            if (fromBalance.compareTo(amountToTransfer) < 0)
            {
                throw new AccountStoreServiceException.InsufficientBalanceException();
            }

            try
            {
                userBalances.put(from, fromBalance.subtract(amountToTransfer));
                userBalances.put(to, toBalance.add(amountToTransfer));

                return null;
            } catch (Exception e)
            {
                userBalances.put(from, fromBalance);
                userBalances.put(to, toBalance);
                throw e;
            }
        });
    }

    @Override
    public Future<Void> initiateWithdrawalToAddress(String withdrawalId, String from, String toAddress, BigDecimal amountToWithdraw)
    {
        return task(() ->
        {
            if (activeWithdrawals.containsKey(withdrawalId))
            {
                throw new AccountStoreServiceException.WithdrawalAlreadyBeingProcessedException();
            }

            if (!userBalances.containsKey(from))
            {
                throw new AccountStoreServiceException.UserDoesNotExistException(from);
            }

            if (amountToWithdraw.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new AccountStoreServiceException.AmountCannotBeBelowZeroException();
            }

            final BigDecimal fromBalance = userBalances.get(from);

            if (fromBalance.compareTo(amountToWithdraw) < 0)
            {
                throw new AccountStoreServiceException.InsufficientBalanceException();
            }

            final WithdrawalService.WithdrawalId withdrawalRequestId = new WithdrawalService.WithdrawalId(UUID.randomUUID());
            withdrawalService.requestWithdrawal(
                    withdrawalRequestId,
                    new WithdrawalService.Address(toAddress),
                    new WithdrawalService.Amount(amountToWithdraw.unscaledValue().longValue(), amountToWithdraw.scale())
            );

            // withdrawal request has succeeded, we can now proceed to update state
            // there's no possible avenue or need for error handling if it fails
            // so we won't

            final ActiveWithdrawal activeWithdrawal = new ActiveWithdrawal(
                    withdrawalId,
                    withdrawalRequestId
            );

            activeWithdrawals.put(withdrawalId, activeWithdrawal);

            // subtract pending balance - NOTE: should we track separately?
            userBalances.put(from, fromBalance.subtract(amountToWithdraw));

            return null;
        });
    }

    @Override
    public Future<WithdrawalState> checkWithdrawalStatus(String withdrawalId)
    {
        return task(() ->
        {
            throw new UnsupportedOperationException();
        });
    }

    private record ActiveWithdrawal(
            String withdrawalId,
            WithdrawalService.WithdrawalId withdrawalRequestId
    )
    {
    }
}
