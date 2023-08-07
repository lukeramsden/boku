package com.lukeramsden.boku.infrastructure.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

public final class VertxLifecycle implements AutoCloseable
{
    private static final int VERTX_EVENT_LOOP_POOL_SIZE = 4;
    private static final int VERTX_EXECUTION_TIME_WARNING_THRESHOLD_SECS = 3;
    public static final int VERTX_WORKER_POOL_SIZE = 4;

    private final Vertx vertx;

    private VertxLifecycle(final Vertx vertx)
    {
        this.vertx = vertx;
    }

    public static VertxLifecycle launch()
    {
        final VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(VERTX_WORKER_POOL_SIZE)
                .setEventLoopPoolSize(VERTX_EVENT_LOOP_POOL_SIZE)
                .setWarningExceptionTime(TimeUnit.SECONDS.toNanos(VERTX_EXECUTION_TIME_WARNING_THRESHOLD_SECS))
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(VERTX_EXECUTION_TIME_WARNING_THRESHOLD_SECS));

        return new VertxLifecycle(Vertx.vertx(vertxOptions));
    }

    @Override
    public void close()
    {
        vertx.close();
    }

    public Vertx vertx()
    {
        return vertx;
    }
}
