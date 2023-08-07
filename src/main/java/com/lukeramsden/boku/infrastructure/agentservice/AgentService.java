package com.lukeramsden.boku.infrastructure.agentservice;

import io.vertx.core.Future;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.util.function.Function;

public abstract class AgentService implements Agent
{
    private final ManyToOneConcurrentArrayQueue<Runnable> tasks = new ManyToOneConcurrentArrayQueue<>(128);

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

    protected <T> Future<T> task(final Function<Void, T> task)
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
