package com.lukeramsden.boku.infrastructure.clock;

import org.agrona.concurrent.EpochNanoClock;
import org.agrona.concurrent.SystemEpochNanoClock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class SystemEpochClock implements EpochClock
{
    private final EpochNanoClock clock = new SystemEpochNanoClock();

    @Override
    public long currentTimeMillis()
    {
        return NANOSECONDS.toMillis(currentTimeNanos());
    }

    @Override
    public long currentTimeMicros()
    {
        return NANOSECONDS.toMicros(currentTimeNanos());
    }

    @Override
    public long currentTimeNanos()
    {
        return clock.nanoTime();
    }
}
