package de.deverado.framework.core.problemreporting;

import org.slf4j.Logger;

public interface ProblemReporter {

    void debug(Logger log, String msg, Object... objects);

    void info(Logger log, String msg, Object... objects);

    void warn(Logger log, String msg, Object... objects);

    void error(Logger log, String msg, Object... objects);

    void fatal(Logger log, String msg, Object... objects);

    RuntimeException logErrorAndMakeException(Logger log, Exception e,
                                              String msg);

    void logError(Logger log, Exception e, String msg);

    RuntimeException logErrorAndMakeException(Logger log, String msg,
                                              Object... objects);

    void logError(Logger log, String msg, Object... objects);
}
