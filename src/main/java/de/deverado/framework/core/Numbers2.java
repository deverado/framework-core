package de.deverado.framework.core;/*
 * Copyright Georg Koester 2012. All rights reserved.
 */

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Numbers2 {

    /**
     * In contrast to {@link Integer#toBinaryString(int)} shows leading zeros.
     *
     * @param i
     * @return
     */
    public static String toBinaryString(int i) {
        StringBuilder o = new StringBuilder(32);
        for (int j = 31; j >= 0; j--) {
            o.append(((i >> j) & 1));
        }
        return o.toString();
    }

    public static String toBinaryString(long l) {
        StringBuilder o = new StringBuilder(64);
        for (int j = 63; j >= 0; j--) {
            o.append(((l >> j) & 1));
        }
        return o.toString();
    }
}
