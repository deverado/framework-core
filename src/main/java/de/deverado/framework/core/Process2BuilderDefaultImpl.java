package de.deverado.framework.core;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class Process2BuilderDefaultImpl extends Process2Builder {

    ListeningExecutorService defaultExec;

    public Process2BuilderDefaultImpl() {
        this(MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                100, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>())));
    }

    public Process2BuilderDefaultImpl(ListeningExecutorService defaultExec) {
        this.defaultExec = defaultExec;
    }

    @Override
    protected ListeningExecutorService getDefaultExecutor() {
        return defaultExec;
    }
}
