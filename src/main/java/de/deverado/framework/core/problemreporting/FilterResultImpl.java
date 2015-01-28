package de.deverado.framework.core.problemreporting;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FilterResultImpl implements FilterResult {

    private Treatment treatment;

    private Class<? extends Throwable> mapTarget;

    public FilterResultImpl(Treatment t, Class<? extends Throwable> mapTarget) {
        this.treatment = t;
        this.mapTarget = mapTarget;
    }

    @Override
    public Treatment getTreatment() {
        return treatment;
    }

    @Override
    public Class<? extends Throwable> getMapTarget() {
        return mapTarget;
    }
}
