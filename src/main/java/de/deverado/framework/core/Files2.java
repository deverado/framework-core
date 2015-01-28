package de.deverado.framework.core;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ParametersAreNonnullByDefault
public class Files2 {

    /**
     * Splits the path ignoring empty directories (contrary to java.io.File).
     * Example: /a//b results in ["a","b"] and not in ["", "a", "", "b"].
     * 
     * But: a/ /b results in ["a", " ", "b"]
     * 
     * @param path
     * @return
     */
    public static String[] splitPath(@Nullable String path) {
        if (StringUtils.isBlank(path)) {
            return new String[0];
        }
        return splitPath(new File(path));
    }

    /**
     * @see #splitPath(String)
     * @param path
     * @return
     */
    public static String[] splitPath(@Nullable File path) {
        List<String> l = Lists.newArrayList();
        File curr = path;
        while (curr != null) {

            String name = curr.getName();
            if (!Strings.isNullOrEmpty(name)) {

                l.add(name);
            }
            curr = curr.getParentFile();
        }
        Collections.reverse(l);
        return l.toArray(new String[l.size()]);
    }

    private static final Joiner PATH_JOINER = Joiner.on("/").skipNulls();

    public static String joinPath(@Nullable String a, @Nullable String b, String... rest) {
        return joinPath(a, b, rest, 0, rest.length);
    }

    public static String joinPath(@Nullable String a, String... rest) {
        return joinPath(a, null, // skipNulls on joiner allows for this
                rest);
    }

    public static String joinPath(@Nullable String a, String[] rest, int restStart,
            int restLen) {
        return joinPath(a, null, rest, restStart, restLen);
    }

    private static final Predicate<Object> pathMemberPred = new Predicate<Object>() {

        @Override
        public boolean apply(Object input) {
            if (Strings.isNullOrEmpty(input == null ? null : input.toString())) {
                return false;
            }
            return true;
        }
    };

    public static String joinPath(@Nullable String a, @Nullable String b, String[] rest,
            int restStart, int restLen) {
        Iterator<Object> iterator = Iterators.concat(
                Iterators.singletonIterator(a),
                b != null ? Iterators.singletonIterator(b) : Collections
                        .emptyIterator(), //
                Collections3.iterForArray(rest, restStart, restLen));

        iterator = Iterators.filter(iterator, pathMemberPred);
        return PATH_JOINER.join(iterator);
    }
}
