package com.lukeramsden.boku.service.accountstore;

import com.lukeramsden.boku.infrastructure.agentservice.AgentService;
import com.lukeramsden.boku.infrastructure.clock.EpochClock;
import com.lukeramsden.boku.service.withdrawal.WithdrawalService;
import io.vertx.core.Future;
import org.agrona.collections.Hashing;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
    private static final Duration WITHDRAWAL_STATE_POLL_INTERVAL = Duration.ofMillis(500);
    private final WithdrawalService withdrawalService;

    private final Map<String, BigDecimal> userBalances;
    private final Map<String, WithdrawalDetail> withdrawals;
    private final List<String> activeWithdrawals;

    public AccountStoreServiceStub(EpochClock clock, WithdrawalService withdrawalService)
    {
        super(clock);
        this.withdrawalService = withdrawalService;
        this.userBalances = new Object2ObjectHashMap<>(
                ObjectHashSet.DEFAULT_INITIAL_CAPACITY,
                Hashing.DEFAULT_LOAD_FACTOR,
                true
        );
        this.withdrawals = new Object2ObjectHashMap<>(
                ObjectHashSet.DEFAULT_INITIAL_CAPACITY,
                Hashing.DEFAULT_LOAD_FACTOR,
                true
        );
        this.activeWithdrawals = new ArrayList<>();
        scheduleTaskIn(this::pollWithdrawalStates, WITHDRAWAL_STATE_POLL_INTERVAL);
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
            if (withdrawals.containsKey(withdrawalId))
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

            final WithdrawalDetail withdrawalDetail = new WithdrawalDetail(
                    withdrawalId,
                    withdrawalRequestId,
                    WithdrawalState.PROCESSING
            );

            withdrawals.put(withdrawalId, withdrawalDetail);
            activeWithdrawals.add(withdrawalId);

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
            if (!withdrawals.containsKey(withdrawalId))
            {
                throw new AccountStoreServiceException.WithdrawalDoesNotExistException();
            }

            return withdrawals.get(withdrawalId).state;
        });
    }

    private void pollWithdrawalStates()
    {
        final List<String> withdrawalsToRemove = new ArrayList<>();

        for (final String withdrawalId : activeWithdrawals)
        {
            final WithdrawalDetail withdrawalDetail = withdrawals.get(withdrawalId);

            final WithdrawalService.WithdrawalState latestRequestState = withdrawalService.getRequestState(
                    withdrawalDetail.withdrawalRequestId()
            );

            switch (latestRequestState)
            {
                case PROCESSING ->
                {
                }
                case COMPLETED ->
                {
                    withdrawalsToRemove.add(withdrawalId);
                    withdrawals.put(
                            withdrawalId,
                            withdrawalDetail.withUpdatedState(WithdrawalState.COMPLETED)
                    );
                }
                case FAILED ->
                {
                    withdrawalsToRemove.add(withdrawalId);
                    withdrawals.put(
                            withdrawalId,
                            withdrawalDetail.withUpdatedState(WithdrawalState.FAILED)
                    );
                }
            }
        }

        for (String withdrawalId : withdrawalsToRemove)
        {
            activeWithdrawals.remove(withdrawalId);
        }

        // Repeat again
        scheduleTaskIn(this::pollWithdrawalStates, WITHDRAWAL_STATE_POLL_INTERVAL);
    }

    private record WithdrawalDetail(
            String withdrawalId,
            WithdrawalService.WithdrawalId withdrawalRequestId,
            WithdrawalState state
    )
    {
        WithdrawalDetail withUpdatedState(WithdrawalState newState)
        {
            return new WithdrawalDetail(withdrawalId, withdrawalRequestId, newState);
        }
    }
}
