package de.deverado.framework.core.problemreporting;

import javax.annotation.Nullable;

/**
 * If multiple filters apply the treatments are compared by priority.
 */
public interface ProblemReporterFilter {

    /**
     * 
     * @param msg
     * @param t
     * @return may return <code>null</code>
     */
    FilterResult filter(String msg, @Nullable Throwable t);
}
