package de.deverado.framework.core.problemreporting;/*
 * Copyright Georg Koester 2012-15. All rights reserved.
 */

public enum Treatment {

    MAP_ONLY(5), LOG(50), LOG_AND_REPORT(100);

    private final int prio;

    private Treatment(int prio) {
        this.prio = prio;
    }

    public int getPrio() {
        return prio;
    }

    public final int comparePrioTo(Treatment o) {
        return this.getPrio() - o.getPrio();
    }
}
