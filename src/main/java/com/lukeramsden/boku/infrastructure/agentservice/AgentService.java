package com.lukeramsden.boku.infrastructure.agentservice;

import com.lukeramsden.boku.infrastructure.clock.EpochClock;
import io.vertx.core.Future;
import org.agrona.DeadlineTimerWheel;
import org.agrona.collections.Hashing;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class AgentService implements Agent
{
    public static final int EXPIRY_LIMIT = 128;
    private final ManyToOneConcurrentArrayQueue<Runnable> tasks;
    private final EpochClock clock;
    private final DeadlineTimerWheel deadlineTimerWheel;
    private final Long2ObjectHashMap<Runnable> scheduledTasks;

    protected AgentService(EpochClock clock)
    {
        this.clock = clock;

        tasks = new ManyToOneConcurrentArrayQueue<>(EXPIRY_LIMIT);
        scheduledTasks = new Long2ObjectHashMap<>();
        deadlineTimerWheel = new DeadlineTimerWheel(TimeUnit.MILLISECONDS, clock.currentTimeMillis(), 16, 16);
    }

    @Override
    public String roleName()
    {
        return "account-store-service-stub";
    }

    @Override
    public int doWork()
    {
        return tasks.drain(Runnable::run) + pollTimerExpiry();
    }

    public int pollTimerExpiry()
    {
        final long nowMs = clock.currentTimeMillis();
        int expired = 0;

        do
        {
            expired += deadlineTimerWheel.poll(
                    nowMs,
                    this::handleExpiredTimer,
                    EXPIRY_LIMIT
            );
        }
        while (expired <= EXPIRY_LIMIT && deadlineTimerWheel.currentTickTime() < nowMs);

        return expired;
    }

    private boolean handleExpiredTimer(TimeUnit timeUnit, long now, long timerId)
    {
        final Runnable runnable = scheduledTasks.remove(timerId);

        if (runnable != null)
        {
            runnable.run();
        }

        return true;
    }

    protected <R> Future<R> task(final Task<R> task)
    {
        return Future.future(fut ->
                tasks.add(() ->
                {
                    try
                    {
                        fut.complete(task.run());
                    } catch (Exception e)
                    {
                        fut.fail(e);
                    }
                })
        );
    }

    protected void scheduleTaskIn(final Runnable task, final Duration timeFromNow)
    {
        final long timerId = deadlineTimerWheel.scheduleTimer(clock.currentTimeMillis() + timeFromNow.toMillis());
        scheduledTasks.put(timerId, task);
    }

    protected interface Task<R>
    {
        R run() throws Exception;
    }
}
