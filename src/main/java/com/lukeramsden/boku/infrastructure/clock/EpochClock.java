package com.lukeramsden.boku.infrastructure.clock;

public interface EpochClock
{
    long currentTimeMillis();

    long currentTimeMicros();

    long currentTimeNanos();
}
