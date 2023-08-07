package com.lukeramsden.boku.service.accountstore;

import io.vertx.core.Future;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
class AccountStoreServiceStub implements AccountStoreService, Agent
{
    private final ManyToOneConcurrentArrayQueue<Runnable> tasks = new ManyToOneConcurrentArrayQueue<>(128);

    private Set<String> users = new ObjectHashSet<>();
    private Map<String, BigDecimal> userBalances = new Object2ObjectHashMap<>();

    @Override
    public Future<Void> adminSetUserBalance(String username, BigDecimal balance)
    {
        return runTask(__ ->
        {
            users.add(username);
            userBalances.put(username, new BigDecimal(balance.unscaledValue(), balance.scale()));
            return null;
        });
    }

    @Override
    public int doWork()
    {
        return tasks.drain(Runnable::run);
    }

    @Override
    public String roleName()
    {
        return "account-store-service-stub";
    }

    private <T> Future<T> runTask(final Function<Void, T> task)
    {
        return Future.future(fut ->
                tasks.add(() ->
                {
                    try
                    {
                        fut.complete(task.apply(null));
                    } catch (Exception e)
                    {
                        fut.fail(e);
                    }
                })
        );
    }
}
