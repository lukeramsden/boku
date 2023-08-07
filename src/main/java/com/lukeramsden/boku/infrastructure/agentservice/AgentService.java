package com.lukeramsden.boku.infrastructure.agentservice;

import io.vertx.core.Future;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

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

    protected interface Task<R>
    {
        R run() throws Exception;
    }
}
