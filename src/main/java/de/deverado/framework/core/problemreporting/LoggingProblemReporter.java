package de.deverado.framework.core.problemreporting;

import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class LoggingProblemReporter implements ProblemReporter {

    @Override
    public void debug(Logger log, String msg, Object... objects) {
        log.debug(msg, objects);
    }

    @Override
    public void info(Logger log, String msg, Object... objects) {
        log.info(msg, objects);
    }

    @Override
    public void warn(Logger log, String msg, Object... objects) {
        log.warn(msg, objects);
    }

    @Override
    public void error(Logger log, String msg, Object... objects) {
        log.error(msg, objects);
    }

    @Override
    public void fatal(Logger log, String msg, Object... objects) {
        log.error(msg, objects);
    }

    public RuntimeException logErrorAndMakeException(Logger log, Exception e,
            String msg) {
        logError(log, e, msg);
        if (e != null) {
            return new RuntimeException(msg, e);
        } else {
            return new RuntimeException(msg);
        }
    }

    public void logError(Logger log, Exception e, String msg) {
        if (e != null) {
            error(log, msg + " ex={}", e.getClass().getSimpleName(), e);
        } else {
            error(log, msg);
        }
    }

    @Override
    public RuntimeException logErrorAndMakeException(Logger logParam,
            String msg, Object... objects) {
        logParam.error(msg, objects);

        FormattingTuple tuple = MessageFormatter.arrayFormat(msg, objects);

        Throwable cause = tuple.getThrowable();
        if (cause == null) {
            return new RuntimeException(tuple.getMessage());
        } else {
            return new RuntimeException(tuple.getMessage(), cause);
        }
    }

    @Override
    public void logError(Logger log, String msg, Object... objects) {
        log.error(msg, objects);
    }
}
