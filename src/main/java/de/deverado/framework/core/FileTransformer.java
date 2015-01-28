package de.deverado.framework.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Handles temporary files if a file is to be replaced in a transformation.
 */
public class FileTransformer implements Closeable {

    private static final Logger log = LoggerFactory
            .getLogger(FileTransformer.class);

    private File src;

    private File target;

    private File intermediaryTarget;

    private boolean started = false;

    private boolean finishedSuccessfully;

    public FileTransformer() {
    }

    public FileTransformer(File src, File target) {
        setSrc(src);
        setTarget(target);
    }

    /**
     * Don't forget to call {@link #finishedSuccessfully()} if successful and
     * {@link #close()} in a finally to ensure cleaning of temporaries.
     * 
     * This should better not be done multiple times with the same
     * {@link FileTransformer}. Create a new one every time a modification is
     * done.
     * 
     * @param m
     * @throws Exception
     */
    public void modify(FileTransformation m) throws Exception {
        if (m == null)
            return;
        prepare();
        m.transform(getSrc(), intermediaryTarget);
    }

    public void finishedSuccessfully() {
        finishedSuccessfully = true;
        close();
    }

    private void prepare() throws IOException {
        Preconditions.checkState(started == false,
                "Started already, use a new transformer please");

        if (!started) {
            // equalize input. src and target both set afterwards.
            if (getSrc() == null) {
                if (getTarget() == null) {
                    throw new IllegalStateException(
                            "Src or target must be present");
                }
                setSrc(getTarget());
            }

            if (getTarget() == null) {
                setTarget(getSrc());
            }

            started = true;

            if (getSrc().equals(getTarget())) {
                String extension = Files
                        .getFileExtension(getTarget().getName());
                intermediaryTarget = File.createTempFile("fileTrans", "."
                        + extension, getTarget().getParentFile());
            } else {
                intermediaryTarget = getTarget();
            }
        }
    }

    /**
     * Deletes the temporary. Moves the changed file.
     * 
     * @see java.io.Closeable#close()
     */
    public void close() {
        if (started) {
            if (intermediaryTarget != null
                    && !intermediaryTarget.equals(getTarget())) {

                if (finishedSuccessfully) {
                    try {
                        getTarget().delete();
                    } catch (SecurityException e) {
                        // clean up
                        try {
                            intermediaryTarget.delete();
                        } catch (Exception e2) {
                            log.error("Cannot clean up temporary file "
                                    + "after exception: {}", e.toString(), e2);
                        }
                        throw e;
                    }
                    // if there is an exception here no cleaning must happen:
                    intermediaryTarget.renameTo(getTarget());

                } else {
                    intermediaryTarget.delete();
                }
            }
            intermediaryTarget = null;
            started = false;
        }
    }

    public File getSrc() {
        return src;
    }

    public void setSrc(File src) {
        checkStarted();
        this.src = src;
    }

    private void checkStarted() {
        Preconditions
                .checkState(!started, "cannot change this after modifying");
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        checkStarted();
        this.target = target;
    }

    public File modifyAsync() throws IOException {
        prepare();
        return intermediaryTarget;
    }

}
