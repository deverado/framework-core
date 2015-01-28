package de.deverado.framework.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.lang.reflect.Array;
import java.util.*;

@ParametersAreNonnullByDefault
public class Collections3 {

    public static boolean isEmptyOrNull(@Nullable Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static <T> Iterator<T> iterForArray(T[] arr, int start, int len) {
        UnmodifiableIterator<T> iterator = Iterators.forArray(arr);
        if (start == 0 && len == arr.length) {
            return iterator;
        }
        Iterators.advance(iterator, start);
        return Iterators.limit(iterator, len);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T[] flatten(Class<T> clazz, @Nullable T[]... input) {
        if (input == null || input.length < 1) {
            return (T[]) Array.newInstance(clazz, 0);
        }
        int requiredSize = 0;
        for (int i = 0; i < input.length; i++) {
            T[] component = input[i];
            if (component != null) {
                requiredSize += component.length;
            }
        }

        T[] retval = (T[]) Array.newInstance(clazz, requiredSize);
        int nextFree = 0;
        for (int i = 0; i < input.length; i++) {
            T[] component = input[i];
            if (component != null) {
                System.arraycopy(component, 0, retval, nextFree, component.length);
                nextFree += component.length;
            }
        }
        return retval;
    }

    public static <T> List<T> filterNullsAndShrinkMaybe(@Nullable List<T> toFilter, boolean shrink) {
        if (toFilter == null) {
            return Collections.emptyList();
        }
        return filterNullsAndShrinkMaybe(toFilter.iterator(), toFilter.size(), shrink);
    }

    /**
     * @param shrink reallocates array if more than 10% of nulls were found.
     */
    public static <T> List<T> filterNullsAndShrinkMaybe(@Nullable T[] toFilter, boolean shrink) {
        if (toFilter == null) {
            return Collections.emptyList();
        }
        return filterNullsAndShrinkMaybe(Iterators.forArray(toFilter), toFilter.length, shrink);
    }

    /**
     * @param size   length of iter, -1 means ignored
     * @param shrink reallocates array if more than 10% of nulls were found.
     */
    public static <T> List<T> filterNullsAndShrinkMaybe(@Nullable Iterator<T> toFilter, int size, boolean shrink) {
        if (toFilter == null || !toFilter.hasNext()) {
            return Collections.emptyList();
        }
        int nullsFound = 0;
        List<T> retval = size < 0 ? new ArrayList<T>() : new ArrayList<T>(size);
        while (toFilter.hasNext()) {
            T curr = toFilter.next();
            if (curr != null) {
                retval.add(curr);
            } else {
                nullsFound++;
            }
        }

        if (shrink) {
            float percentageOfNulls = ((float) nullsFound) / retval.size();
            if (size < 0 || percentageOfNulls >= 0.1) {
                ArrayList<T> temp = new ArrayList<>(retval.size() - nullsFound);
                temp.addAll(retval);
                retval = temp;
            }
        }
        return retval;
    }
}
