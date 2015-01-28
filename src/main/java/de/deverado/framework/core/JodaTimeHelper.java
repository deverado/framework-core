package de.deverado.framework.core;/*
 * Copyright Georg Koester 2012-15. All rights reserved.
 */

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JodaTimeHelper {

    /**
     * A bit arbitrary joda-compatible min date long. JodaTime instant doesn't work with Long.MIN/MAX.
     */
    public static final long ARBITRARY_MIN_DATE_MS = new DateTime(-145075054, 1, 1, 0, 0, 0,
            ISOChronology.getInstanceUTC()).getMillis();

    /**
     * A bit arbitrary joda-compatible max date long. JodaTime instant doesn't work with Long.MIN/MAX.
     */
    public static final long ARBITRARY_MAX_DATE_MS =
            ARBITRARY_MIN_DATE_MS + -1 * ARBITRARY_MIN_DATE_MS + -1 * ARBITRARY_MIN_DATE_MS;

    /**
     * JodaTime DateTime must be created with a timezone or a default is used.
     */
    public static DateTime utcDateTimeFor(int year, int monthOfYear, int dayOfMonth, int hour, int minute, int second,
                                          int millisecond) {
        return new DateTime(year, monthOfYear, dayOfMonth, hour, minute, second, millisecond,
                ISOChronology.getInstanceUTC());
    }

    /**
     * JodaTime DateTime must be created with a timezone or a default is used.
     */
    public static DateTime utcDateTimeFor(int year, int monthOfYear, int dayOfMonth) {
        return utcDateTimeFor(year, monthOfYear, dayOfMonth, 0, 0, 0, 0);
    }

    public static DateTime utcDateTimeFor(long millis) {
        return new DateTime(millis, ISOChronology.getInstanceUTC());
    }
}
