package de.deverado.framework.core;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@ParametersAreNonnullByDefault
public class Process2 {

    private static final Logger log = LoggerFactory.getLogger(Process2.class);

    private Process process;

    private ListenableFuture<String> inputReaderFuture;

    private OutputStream processOutputTarget;

    private Charset charsetForErrors = Charsets.UTF_8;

    private ListenableFuture<Integer> processExitvalFuture;

    public Process2() {
    }

    /**
     * Ensures that process is started with its output being copied to
     * processOutputTarget. Exceptions during reading are written to that
     * stream, too (except EOF).
     * 
     * @param exec
     *            used to get two threads, one for reading the output and
     *            copying it to processOutputTarget and one for waiting on the
     *            process and servicing the processResultFuture.
     * @param p
     * @param processOutputTargetParam
     *            defaults to {@link ByteArrayOutputStream}
     * @return this
     */
    public Process2 init(final Process p, final ListeningExecutorService exec,
            @Nullable OutputStream processOutputTargetParam) {

        Preconditions.checkState(inputReaderFuture == null, "already running");

        if (processOutputTargetParam == null) {
            processOutputTargetParam = new ByteArrayOutputStream();
        }

        this.processOutputTarget = processOutputTargetParam;
        this.process = p;

        Runnable inputReader = new Runnable() {
            @Override
            public void run() {
                boolean threw = true;
                try {

                    new ByteSource() {

                        @Override
                        public InputStream openStream() throws IOException {
                            return p.getInputStream();
                        }

                    }.copyTo(processOutputTarget);

                    threw = false;
                } catch (EOFException eof) {
                    log.debug("Got eof on {}, closing output", this);
                    // ok, close normally
                    threw = false;
                } catch (IOException e) {
                    String exstr = Throwables.getStackTraceAsString(e);
                    try {
                        processOutputTarget.write(exstr
                                .getBytes(getCharsetForErrors()));
                    } catch (Exception e2) {
                        log.warn("Cannot write error to "
                                + "process {} output because: {}. "
                                + "Dumping error here:", this, e2.getMessage(),
                                e);
                    }
                } finally {
                    try {
                        Closeables.close(processOutputTarget, threw);
                    } catch (IOException e) {
                        log.warn("Failed to close output of process {}", this,
                                e);
                    }
                }
            }
        };

        this.inputReaderFuture = exec.submit(inputReader, "done");

        Callable<Integer> exitvalCallable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                while (true) {
                    try {

                        return getProcess().waitFor();

                    } catch (InterruptedException e) {
                        if (exec.isShutdown()) {
                            throw e;
                        } else {
                            log.debug("Ignoring interruption if executor isn't shutting down");
                        }
                    }
                }
            }
        };

        this.processExitvalFuture = exec.submit(exitvalCallable);

        return this;
    }

    public Charset getCharsetForErrors() {
        return charsetForErrors;
    }

    /**
     * The charset to dump errors into processOutputTarget. Defaults to UTF-8.
     *
     */
    public void setCharsetForErrors(Charset charsetForErrors) {
        this.charsetForErrors = charsetForErrors;
    }

    public Process getProcess() {
        return process;
    }

    public ListenableFuture<String> getInputReaderFuture() {
        return inputReaderFuture;
    }

    public ListenableFuture<Integer> getProcessExitvalFuture() {
        return processExitvalFuture;
    }

    /**
     * Beware of synchronization if accessing this while
     * {@link #getInputReaderFuture()} isn't finished. Default
     * {@link ByteArrayOutputStream} is synchronized and can be accessed.
     *
     */
    public OutputStream getProcessOutputTarget() {
        return processOutputTarget;
    }

    public String getProcessOutputAssumingDefaultOutputTargetAsUTF8String() {
        try {
            return ((ByteArrayOutputStream) getProcessOutputTarget())
                    .toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getProcessOutputAssumingDefaultOutputTarget() {
        return ((ByteArrayOutputStream) getProcessOutputTarget()).toByteArray();
    }

    @SuppressWarnings("unchecked")
    public ListenableFuture<List<Object>> getCombinedFuture() {
        return Futures.allAsList(Arrays
                .<ListenableFuture<Object>> asList(new ListenableFuture[] {
                        getProcessExitvalFuture(), getInputReaderFuture() }));
    }
}
