package de.deverado.framework.core.problemreporting;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.deverado.framework.core.ExceptionsHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Allows configuring filters via property {@code problemReporter.filterConfig}
 * in your {@link de.deverado.framework.core.propertyconfig.PropsConfigurator}.
 */
public class TextConfigProblemReporterFilter implements ProblemReporterFilter {

    private static final Logger log = LoggerFactory
            .getLogger(TextConfigProblemReporterFilter.class);

    private Map<Class<?>, FilterResultWithTarget> causeCrawlingTargets = Maps
            .newHashMap();
    private Map<Class<?>, FilterResultWithTarget> directTargets = Maps
            .newHashMap();

    public void init(@Nullable String filterConfig) {
        if (!StringUtils.isBlank(filterConfig)) {
            Iterable<String> configs = Splitter.on(";").omitEmptyStrings()
                    .trimResults().split(filterConfig);

            for (String config : configs) {
                analyseSingleFilterConfig(config);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void analyseSingleFilterConfig(String config) {

        Iterable<String> split = Splitter.on(":").omitEmptyStrings()
                .trimResults().split(config);
        String[] array = Iterables.toArray(split, String.class);
        if (array.length < 2) {
            throw new RuntimeException(
                    "Config needs at least two parts seperated by ':': "
                            + config);
        }
        Class<? extends Throwable> filterAppliesTo;
        try {
            filterAppliesTo = (Class<? extends Throwable>) getClass()
                    .getClassLoader().loadClass(array[0]);

        } catch (Exception e) {
            throw new RuntimeException("Not a Throwable: " + array[0], e);
        }
        String restConfig = array[1];

        Treatment tmt = null;
        Set<String> flags = Sets.newHashSet();
        Class<? extends Throwable> mapTarget = null;
        for (String val : Splitter.on(",").omitEmptyStrings().trimResults()
                .split(restConfig)) {
            if (tmt == null) {
                try {
                    tmt = Treatment.valueOf(val.toUpperCase());
                } catch (IllegalArgumentException iae) {
                    throw new RuntimeException(
                            "Filter config erroneous, right part must start with Treatment: "
                                    + config);
                }
                continue;
            }

            Class<? extends Throwable> class1 = null;
            try {
                class1 = (Class<? extends Throwable>) getClass()
                        .getClassLoader().loadClass(val);
            } catch (Exception e) {
                log.trace("Not a class, treating as flag: {}", val);
            }
            if (class1 != null) {

                try {
                    Throwable instance = ExceptionsHelper.newWithCause(class1,
                            new Exception(), null);
                    log.trace("Tried construction of one of {}: {}", class1,
                            instance != null ? instance.toString() : "null");
                } catch (Exception e) {
                    throw new RuntimeException("Class " + val
                            + " not a subclass of Throwable or "
                            + "not having a (String, Throwable) constructor", e);
                }
                mapTarget = class1;
                continue;
            }

            flags.add(val.toUpperCase());

        }

        if (tmt != null) {
            FilterResultWithTarget resultWithTarget = new FilterResultWithTarget(
                    filterAppliesTo, tmt, mapTarget);
            if (flags.contains("CRAWL_CAUSES")) {
                causeCrawlingTargets.put(filterAppliesTo, resultWithTarget);
            } else {
                directTargets.put(filterAppliesTo, resultWithTarget);
            }
            log.info("Added config {}", config);
        }

    }

    @Override
    public FilterResult filter(String msg, @Nullable Throwable t) {
        if (t == null) {
            return null;
        }
        FilterResultWithTarget retval = directTargets.get(t.getClass());
        if (retval == null && !causeCrawlingTargets.isEmpty()) {

            Throwable currCause = t;
            while (currCause != null) {
                retval = causeCrawlingTargets.get(currCause.getClass());
                if (retval != null) {
                    break;
                }

                currCause = currCause.getCause();
            }
        }
        return retval;
    }

    private static class FilterResultWithTarget extends FilterResultImpl {

        private Class<? extends Throwable> target;

        public FilterResultWithTarget(Class<? extends Throwable> target,
                Treatment t, Class<? extends Throwable> mapTo) {
            super(t, mapTo);
            this.target = target;
        }

        public Class<? extends Throwable> getFilterTarget() {
            return target;
        }
    }

}
