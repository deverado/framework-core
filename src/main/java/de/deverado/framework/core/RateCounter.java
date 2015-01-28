package de.deverado.framework.core;

import java.util.concurrent.atomic.AtomicLong;

public class RateCounter {

    private final long delay;

    private final AtomicLong last = new AtomicLong(0);

    private final AtomicLong counter = new AtomicLong(0);

    private volatile long lastFinishedVal = 0;

    public RateCounter(long countDelayMs) {
        this.delay = countDelayMs;
        last.set(System.currentTimeMillis());

    }

    public void incr() {
        incr(1);
    }

    public void incr(long count) {
        changeIfNecessary();
        counter.incrementAndGet();
    }

    private void changeIfNecessary() {
        long now = System.currentTimeMillis();
        long last2 = last.get();
        if ((now - last2) >= delay) {
            if (last.compareAndSet(last2, last2 + delay)) {
                lastFinishedVal = counter.getAndSet(0);
            }
        }
    }

    public long getLastIntervalCount() {
        return lastFinishedVal;
    }

}
