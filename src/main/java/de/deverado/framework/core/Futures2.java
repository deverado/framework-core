package de.deverado.framework.core;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@ParametersAreNonnullByDefault
public class Futures2 {

    public static <I, V> ListenableFuture<V> always(
            final ListenableFuture<? extends I> left,
            final AsyncFunction<ListenableFuture<I>, ? extends V> transformer) {
        return always(left, transformer, MoreExecutors.directExecutor());
    }

    public static <I, V> ListenableFuture<V> always(
            final ListenableFuture<? extends I> left,
            final AsyncFunction<ListenableFuture<I>, ? extends V> transformer,
            Executor executor) {

        ListenableFutureTask<? extends ListenableFuture<? extends V>> task = ListenableFutureTask.create(
                new Callable<ListenableFuture<? extends V>>() {
                    @Override
                    public ListenableFuture<? extends V> call() throws Exception {
                        return transformer.apply((ListenableFuture<I>)left);
                    }
                });

        left.addListener(task, executor);
        return Futures.dereference(task);
    }

    public static <I, V> ListenableFuture<V> then(
            final ListenableFuture<I> left,
            @Nullable final AsyncFunction<? super I, ? extends V> successTransformer,
            @Nullable final AsyncFunction<Throwable, ? extends V> failTransformer) {

        return then(left, successTransformer, failTransformer, MoreExecutors.directExecutor());
    }

    public static <I, V> ListenableFuture<V> then(
            final ListenableFuture<I> left,
            @Nullable final AsyncFunction<? super I, ? extends V> successTransformer,
            @Nullable final AsyncFunction<Throwable, ? extends V> failTransformer,
            Executor executor) {

        ListenableFutureTask<ListenableFuture<? extends V>> task = ListenableFutureTask.create(
                new Callable<ListenableFuture<? extends V>>() {
                    @Override
                    public ListenableFuture<? extends V> call() throws Exception {
                        try {
                            I result = left.get();
                            if (successTransformer != null) {
                                return successTransformer.apply(result);
                            } else {
                                return Futures.immediateFuture(null);
                            }
                        } catch (ExecutionException ee) {
                            if (failTransformer != null) {
                                return failTransformer.apply(ee.getCause());
                            } else {
                                return Futures.immediateFailedFuture(ee.getCause());
                            }
                        }
                    }
                });

        left.addListener(task, executor);
        return Futures.dereference(task);
    }

    public static <V> ListenableFuture<V> wrapLeavingOriginalNonCancellable(
            @Nullable final ListenableFuture<V> f) {
        if (f == null) {
            return null;
        }

        ListenableFutureTask<V> outer = ListenableFutureTask
                .create(new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return f.get();
                    }
                });
        f.addListener(outer, MoreExecutors.directExecutor());
        return outer;
    }

    public static enum FutureState {
        RUNNING,

        DONE,

        CANCELED,

        FRESH,

        ;
    }

    /**
     *
     * @param lowProcessingLoadExecutor not null - ensure these can be blocked
     * @param transformContext not null - create this as a singleton in your app
     * @param callSpecificTag not null - provide a tag so that multiple callables from different transform calls can be
     *                        linked. E.g. Pair.of(CacheObjectMarker, CacheKey)
     * @param tagSpecificBlockingCall not null - the call that can block.
     * @param <V>
     * @return a future that will finish when the blocking call is finished. Cancelling it will cancel the current
     * execution for all listeners (not a wrapped future {@see #wrapLeavingOriginalNonCancellable}).
     */
    public static <V> ListenableFuture<V> transformWithGroupingByTag(
            ListeningExecutorService lowProcessingLoadExecutor,
            final ConcurrentHashMap<Object, ListenableFuture<?>> transformContext,
            final Object callSpecificTag,
            final Callable<V> tagSpecificBlockingCall) {

        ListenableFuture<V> listenableFuture;
        boolean created = false;
        do {
            // listenable future still not valid at beginning
            listenableFuture =
                    (ListenableFuture<V>) transformContext.get(callSpecificTag);
            if (listenableFuture == null) {
                listenableFuture = SettableFuture.create();
                ListenableFuture<?> existingFuture = transformContext.putIfAbsent(callSpecificTag,
                        listenableFuture);
                if (existingFuture != null) {
                    continue; // need to check again - might be MARKER
                } else {
                    created = true;
                }
            }
            break;
        } while (true);
        // listenableFuture was assigned - either created by us or existing value.

        if (created) {
            final SettableFuture<V> myCreatedFuture = (SettableFuture<V>) listenableFuture;
            // first to set this, so ensure removal:
            listenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    transformContext.remove(callSpecificTag, myCreatedFuture);
                }
            }, MoreExecutors.directExecutor());

            // first does the call:
            try {
                lowProcessingLoadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            myCreatedFuture.set(tagSpecificBlockingCall.call());
                        } catch (Exception e) {
                            myCreatedFuture.setException(e);
                        }
                    }
                });
            } catch (Exception e) {
                myCreatedFuture.setException(e);
            }
        }
        return listenableFuture;
    }
}
