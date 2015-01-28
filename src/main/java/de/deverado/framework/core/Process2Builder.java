package de.deverado.framework.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * There is a default implementation for guice envs in the guice enabled
 * package.
 */
public abstract class Process2Builder {

    private Process p;

    private ListeningExecutorService executor;

    private OutputStream processOutputTarget;

    private Logger debugLogTo;

    public Process2 build() {
        Preconditions.checkState(p != null, "no Process set");
        return build(p);
    }

    public Process2 build(Process p) {
        this.p = p;
        Process2 process2 = new Process2();
        process2.init(p, getExecutor(), getProcessOutputTarget());

        initDebugLogIfSet(process2);

        return process2;
    }

    private void initDebugLogIfSet(final Process2 process2) {
        if (debugLogTo != null) {
            process2.getCombinedFuture().addListener(new Runnable() {
                @Override
                public void run() {

                    Integer exitval;
                    try {
                        exitval = process2.getProcessExitvalFuture().get();

                        if (debugLogTo.isDebugEnabled()) {
                            String processOut = process2.getProcessOutputAssumingDefaultOutputTargetAsUTF8String();
                            debugLogTo.debug("FFMPEG exitval {} output: {}",
                                    exitval, processOut);
                        }

                    } catch (Exception e) {
                        debugLogTo.debug("Exception while waiting for exitval",
                                e);
                    }

                }
            }, MoreExecutors.directExecutor());
        }
    }

    public Process2 exec(List<String> command) throws IOException {
        return build(new ProcessBuilder(command).redirectErrorStream(true)
                .start());
    }

    public Process2Builder executor(ListeningExecutorService exec) {
        this.executor = exec;
        return this;
    }

    public ListeningExecutorService getExecutor() {
        return this.executor == null ? getDefaultExecutor() : this.executor;
    }

    public Process2Builder processOutputTarget(OutputStream processOutputTarget) {
        this.processOutputTarget = processOutputTarget;
        return this;
    }

    public OutputStream getProcessOutputTarget() {
        if (processOutputTarget == null) {
            processOutputTarget = new ByteArrayOutputStream();
        }
        return processOutputTarget;
    }

    protected abstract ListeningExecutorService getDefaultExecutor();

    public Process2Builder debugLogTo(final Logger log) {
        debugLogTo = log;

        return this;
    }
}
