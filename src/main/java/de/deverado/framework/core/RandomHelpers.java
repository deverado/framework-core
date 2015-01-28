package de.deverado.framework.core;/*
 * Copyright Georg Koester 2012. All rights reserved.
 */

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.HashSet;
import java.util.Random;

@ParametersAreNonnullByDefault
public class RandomHelpers {

    public static String getRandString(Random rand, int s) {
        StringBuilder b = new StringBuilder(s);
        for (int i = 0; i < s; i++) {
            int next = 'a' + rand.nextInt(26);
            b.append((char) next);
        }
        return b.toString();
    }

    private static final String READABLE_STR_SRC_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Creates a string with letters and digits.
     * <p>
     * <ul>
     * <li>4 chars no upper: 1.68 mio with upper 14.8 mio
     * <li>5 chars no upper: 60.5 mio with upper 916 mio
     * <li>6 chars no upper: 2.170 mio with upper 56.800 mio; at 1 try/second
     * need 69 years to make 2.17 mio tries
     * </ul>
     *
     */
    public static String randReadableStr(Random r, int len, boolean lowerOnly) {
        StringBuilder b = new StringBuilder(len);
        int randStart = 0;
        int randLen = READABLE_STR_SRC_TABLE.length();
        if (lowerOnly) {
            randStart = 26; // no upper case
            randLen = READABLE_STR_SRC_TABLE.length() - 26;
        }
        for (int i = 0; i < len; i++) {
            b.append(READABLE_STR_SRC_TABLE.charAt(randStart
                    + r.nextInt(randLen)));
        }
        return b.toString();
    }

    /**
     * simply uses Math.random.
     *
     */
    public static byte[] randBytes(byte[] target, int offset, int count) {
        Preconditions.checkArgument((offset + count) <= target.length);
        for (int i = offset; i < offset + count; i++) {
            target[i] = (byte) (Math.random() * 256);
        }
        return target;
    }

    /**
     * simply uses Math.random.
     *
     */
    public static byte[] randBytes(int count) {
        return randBytes(new byte[count], 0, count);
    }

    /**
     * simply uses Math.random, only 0 and positive ints returned
     *
     */
    public static String randIntStr() {
        return Integer.toString(randPosInt());
    }

    /**
     * simply uses Math.random
     *
     */
    public static int randPosInt() {
        return randPosInt(Integer.MAX_VALUE);
    }

    public static int randPosInt(int max) {
        return Ints.checkedCast((long) (Math.random() * max));
    }

    /**
     * simply uses Math.random, only 0 and positive longs returned
     *
     */
    public static long randPosLong() {
        return randPosLong(Long.MAX_VALUE);
    }

    /**
     * simply uses Math.random, only 0 and positive returned. Possibly not all
     * long values can be returned, check details of java random.
     *
     */
    public static long randPosLong(long max) {
        return (long) (Math.random() * max);
    }

    /**
     * simply uses Math.random
     *
     */
    public static String[] randIntStrings(int count, boolean ensureNoDups) {
        String[] retval = new String[count];
        HashSet<String> set = null;
        if (ensureNoDups) {
            set = Sets.newHashSetWithExpectedSize(count);
        }

        for (int i = 0; i < count; i++) {

            String next = randIntStr();
            if (ensureNoDups) {
                while (set.contains(next)) {
                    next = randIntStr();
                }
                set.add(next);
            }

            retval[i] = next;
        }
        return retval;
    }
}
