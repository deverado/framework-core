package de.deverado.framework.core;/*
 * Copyright Georg Koester 2012. All rights reserved.
 */

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Math2 {

    public static double stddev(int[] counters) {
        int sum = 0;
        for (int i = 0; i < counters.length; i++) {
            sum += counters[i];
        }
        double mean = ((double) sum) / counters.length;

        double sumOfSqMeanDiffs = 0;
        for (int i = 0; i < counters.length; i++) {
            sumOfSqMeanDiffs += Math.pow(counters[i] - mean, 2);
        }
        return Math.sqrt(sumOfSqMeanDiffs / counters.length);
    }
}
