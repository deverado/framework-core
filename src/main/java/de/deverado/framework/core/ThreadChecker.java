package de.deverado.framework.core;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
public class ThreadChecker {

    private static final Logger log = LoggerFactory
            .getLogger(ThreadChecker.class);

    private final Verifier verifier;
    private int currentlyExecuting = 0;

    /**
     * ThreadGroup verifier also requires only one executing thread at a time -
     * but allows threads to go home/be recycled.
     *
     */
    public ThreadChecker(ThreadGroup group) {
        verifier = new GroupVerifier(group);
    }

    /**
     * Creates a thread checker that verifies that the very same instance of
     * Thread is executing.
     */
    public ThreadChecker() {
        this.verifier = new InstanceVerifier();
    }

    public void start() {
        verifier.start();
    }

    public void end() {
        verifier.end();
    }

    protected interface Verifier {
        void start();

        void end();
    }

    protected class GroupVerifier implements Verifier {
        private final ThreadGroup group;

        public GroupVerifier(ThreadGroup group) {
            this.group = group;
            Preconditions.checkArgument(group != null);
        }

        @Override
        public void start() {
            ThreadGroup currGroup = Thread.currentThread().getThreadGroup();
            if (!group.equals(currGroup)) {
                failure(group, currGroup);
            } else {
                normalStart();
            }
        }

        @Override
        public void end() {
            normalEnd();
        }
    }

    protected class InstanceVerifier implements Verifier {
        private Thread lastSeen;

        @Override
        public void start() {
            Thread currentThread = Thread.currentThread();
            if (lastSeen == null) {
                lastSeen = currentThread;
            }
            if (lastSeen != currentThread) {
                failure(lastSeen, currentThread);
                lastSeen = currentThread;
            } else {
                normalStart();
            }
        }

        @Override
        public void end() {
            normalEnd();
        }
    }

    protected void normalStart() {
        ++currentlyExecuting;
    }

    protected void normalEnd() {
        --currentlyExecuting;
    }

    private final AtomicInteger syncVar = new AtomicInteger();

    protected void failure(Object fromWhat, Object toWhat) {

        // sync memory access order by reading an atomic;
        int failCount = syncVar.incrementAndGet();
        int executing = ++currentlyExecuting;

        log.error("Executing thread change detected, from {} to {}, "
                + "tried emergency sync. Fail count: {}", fromWhat, toWhat,
                failCount);
        // don't want to always sync execution. This is only a detection
        // it isn't a reliable thing.
        // the emergency resync will work for a very irregular change,
        // like a refreshing of the calling single-worker pool (eg. when
        // thread is changed every 100000 invocations)

        if (executing > 1) {
            log.error("More than one thread executing handler "
                    + "at same time, non-exact sum: {}!", executing);
        }
    }
}
