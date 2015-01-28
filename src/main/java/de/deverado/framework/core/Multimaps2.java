package de.deverado.framework.core;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class Multimaps2 {

    public static <K, V> SetMultimap<K, V> newHashSetMultimap() {
        return Multimaps.newSetMultimap(new HashMap<K, Collection<V>>(),
                new Supplier<Set<V>>() {
                    @Override
                    public Set<V> get() {
                        return new HashSet<V>();
                    }
                });
    }
}
