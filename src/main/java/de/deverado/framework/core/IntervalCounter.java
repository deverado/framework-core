package de.deverado.framework.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class IntervalCounter {

    private final long delay;

    private final AtomicLong last = new AtomicLong(0);

    private final AtomicLong count = new AtomicLong(0);

    /**
     * Minimum resolution is milliseconds.
     *
     */
    public IntervalCounter(long duration, TimeUnit interval) {
        this.delay = interval.toMillis(duration);
    }

    public long incrementAndGet() {
        return incrementAndGet(1);
    }

    public long incrementAndGet(long incr) {
        long currentTimeMillis = System.currentTimeMillis();
        long localLast = last.get();
        if ((currentTimeMillis - localLast) >= delay) {
            if (last.compareAndSet(localLast, currentTimeMillis)) {
                count.set(incr);
                return incr;
            }
        }
        return count.addAndGet(incr);
    }

    public long get() {
        return incrementAndGet(0);
    }

    public void reset() {
        last.set(System.currentTimeMillis());
        set(0);
    }

    public void set(long value) {
        count.set(value);
    }

}
