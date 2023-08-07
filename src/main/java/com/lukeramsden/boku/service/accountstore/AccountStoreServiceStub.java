package com.lukeramsden.boku.service.accountstore;

import com.lukeramsden.boku.infrastructure.agentservice.AgentService;
import io.vertx.core.Future;
import org.agrona.collections.Hashing;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * AccountStoreServiceStub runs as an agent with a thread-safe public API
 * that queues on to an internal array-backed concurrent queue.
 * <p>
 * This allows the business logic to be single-threaded and to maintain
 * thread cache locality.
 * <p>
 * In a more advanced message-based system that crossed processes (such
 * as one built using Aeron), this would allow us to take advantage of
 * batching for even better performance.
 */
class AccountStoreServiceStub extends AgentService implements AccountStoreService
{
    private final Map<String, BigDecimal> userBalances = new Object2ObjectHashMap<>(ObjectHashSet.DEFAULT_INITIAL_CAPACITY, Hashing.DEFAULT_LOAD_FACTOR, true);

    @Override
    public Future<Void> adminSetUserBalance(String username, BigDecimal balance)
    {
        return task(__ ->
        {
            userBalances.put(username, new BigDecimal(balance.unscaledValue(), balance.scale()));
            return null;
        });
    }

    @Override
    public Future<Optional<BigDecimal>> getUserBalance(String username)
    {
        return task(__ ->
        {
            if (!userBalances.containsKey(username))
            {
                return Optional.empty();
            }

            return Optional.of(userBalances.get(username));
        });
    }
}
