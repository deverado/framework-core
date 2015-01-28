package de.deverado.framework.core.problemreporting;/*
 * Copyright Georg Koester 2012-15. All rights reserved.
 */

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface FilterResult {

    Treatment getTreatment();

    Class<? extends Throwable> getMapTarget();
}
