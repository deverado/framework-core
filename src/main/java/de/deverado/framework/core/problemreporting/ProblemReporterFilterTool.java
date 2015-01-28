package de.deverado.framework.core.problemreporting;


import javax.annotation.Nullable;
import java.util.Set;

public class ProblemReporterFilterTool {

    private Set<ProblemReporterFilter> filters;

    private ProblemReporterFilter[] filterArr;

    private Treatment defaultTreatment = Treatment.LOG_AND_REPORT;

    private FilterResultImpl defaultResult;

    public ProblemReporterFilterTool(Set<ProblemReporterFilter> filters) {
        this.filters = filters;
    }

    public void init() {

        if (filters == null) {
            filterArr = new ProblemReporterFilter[0];
        } else {
            filterArr = filters.toArray(new ProblemReporterFilter[filters
                    .size()]);
        }

        defaultResult = new FilterResultImpl(defaultTreatment, null);
    }

    public FilterResult filter(String msg, @Nullable Throwable t) {

        FilterResult highest = null;
        for (int i = 0; i < filterArr.length; i++) {
            FilterResult result = filterArr[i].filter(msg, t);
            if (result != null) {
                // later filters replace earlier
                if (highest == null
                        || (result.getTreatment().comparePrioTo(highest
                                .getTreatment())) >= 0) {
                    highest = result;
                }
            }
        }

        if (highest != null) {
            return highest;
        } else {
            return defaultResult;
        }
    }
}
