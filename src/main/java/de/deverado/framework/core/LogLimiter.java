package de.deverado.framework.core;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

public class LogLimiter {

    private final Logger log;

    private final IntervalCounter counter;

    public LogLimiter(Logger log, double maxRateMinute) {
        this(log, maxRateMinute, TimeUnit.MINUTES);
    }

    public LogLimiter(Logger log, double maxRate, TimeUnit unit) {
        this.log = log;
        this.counter = new IntervalCounter((long) (unit.toMillis(1) / maxRate),
                TimeUnit.MILLISECONDS);
    }

    public void warn(String msg, Object arg, Throwable ex) {
        if (mayLog()) {
            log.warn(msg, arg, ex);
        }
    }

    public void warn(String msg, Object arg) {
        if (mayLog()) {
            log.warn(msg, arg);
        }
    }

    public void info(String msg, Object arg) {
        if (mayLog()) {
            log.info(msg, arg);
        }
    }

    private boolean mayLog() {
        // only the first in an interval is allowed to be logged
        if (counter.incrementAndGet() == 1) {
            return true;
        }
        return false;
    }

}
