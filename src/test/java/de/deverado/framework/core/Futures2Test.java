package de.deverado.framework.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Futures2Test {

    ListeningExecutorService unlimitedExec;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        if (unlimitedExec != null) {
            unlimitedExec.shutdownNow();
            unlimitedExec = null;
        }
    }

    @Test
    public void testAlwaysGenericsMakeSense() throws ExecutionException, InterruptedException {

        final AtomicInteger innerCheck = new AtomicInteger(0);
        final SettableFuture<Integer> f = SettableFuture.create();

        AsyncFunction<ListenableFuture<Number>, Long> transformer2 = new AsyncFunction<ListenableFuture<Number>, Long>() {
            @Override
            public ListenableFuture<Long> apply(ListenableFuture<Number> input) throws Exception {
                innerCheck.incrementAndGet();
                assertTrue(f.isDone());
                return Futures.immediateFuture((long) 12);
            }
        };
        ListenableFuture<Long> res = Futures2.always(f, transformer2);

        f.set(13);
        assertEquals(1, innerCheck.get());
        assertEquals(Long.valueOf(12), res.get());
        assertEquals(1, innerCheck.get());
    }

    @Test
    public void testAlwaysWorksNormally() throws ExecutionException, InterruptedException {

        final AtomicInteger innerCheck = new AtomicInteger(0);
        final SettableFuture<Boolean> f = SettableFuture.create();
        ListenableFuture<Long> res = Futures2.always(f,
                new AsyncFunction<ListenableFuture<Boolean>, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(ListenableFuture<Boolean> input) throws Exception {
                        innerCheck.incrementAndGet();
                        assertTrue(f.isDone());
                        return Futures.immediateFuture((long) 12);
                    }
                });

        f.set(Boolean.TRUE);
        assertEquals(1, innerCheck.get());
        assertEquals(Long.valueOf(12), res.get());
        assertEquals(1, innerCheck.get());
    }

    @Test
    public void testAlwaysWorksForErrorCase() throws ExecutionException, InterruptedException {

        final AtomicInteger innerCheck = new AtomicInteger(0);
        final SettableFuture<Boolean> f = SettableFuture.create();
        ListenableFuture<Long> res = Futures2.always(f,
                new AsyncFunction<ListenableFuture<Boolean>, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(ListenableFuture<Boolean> input) throws Exception {
                        innerCheck.incrementAndGet();
                        assertTrue(f.isDone());
                        try {
                            f.get();
                            fail("should've thrown");
                        } catch (ExecutionException e) {
                            assertTrue(e.getCause().getMessage().equals("TestEx"));
                        }

                        return Futures.immediateFuture((long) 13);
                    }
                });

        f.setException(new Exception("TestEx"));
        assertEquals(1, innerCheck.get());
        assertEquals(Long.valueOf(13), res.get());
        assertEquals(1, innerCheck.get());
    }

    @Test
    public void testThenWorksNormally() throws ExecutionException, InterruptedException {

        final AtomicInteger innerCheck = new AtomicInteger(0);
        final SettableFuture<Boolean> f = SettableFuture.create();
        ListenableFuture<Long> res = Futures2.then(f,
                new AsyncFunction<Boolean, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(Boolean input) throws Exception {
                        assertTrue(input);
                        innerCheck.incrementAndGet();
                        assertTrue(f.isDone());
                        return Futures.immediateFuture((long) 12);
                    }
                },
                new AsyncFunction<Throwable, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(Throwable input) throws Exception {
                        innerCheck.incrementAndGet();
                        fail();
                        return null;
                    }
                });

        f.set(Boolean.TRUE);
        assertEquals(1, innerCheck.get());
        assertEquals(Long.valueOf(12), res.get());
        assertEquals(1, innerCheck.get());
    }

    @Test
    public void testThenWorksForFailure() throws ExecutionException, InterruptedException {

        final AtomicInteger innerCheck = new AtomicInteger(0);
        final SettableFuture<Boolean> f = SettableFuture.create();
        ListenableFuture<Long> res = Futures2.then(f,
                new AsyncFunction<Boolean, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(Boolean input) throws Exception {
                        innerCheck.incrementAndGet();
                        fail();
                        return null;
                    }
                },
                new AsyncFunction<Throwable, Long>() {
                    @Override
                    public ListenableFuture<Long> apply(Throwable input) throws Exception {
                        innerCheck.incrementAndGet();
                        assertEquals("TestEx", input.getMessage());
                        assertTrue(f.isDone());
                        return Futures.immediateFuture((long) 13);
                    }
                });

        f.setException(new Exception("TestEx"));
        assertEquals(1, innerCheck.get());
        assertEquals(Long.valueOf(13), res.get());
        assertEquals(1, innerCheck.get());
    }

    @Test
    public void testTransformWithGroupingByTag() throws Exception {
        createUnlimitedExec();
        final AtomicInteger innerCheck = new AtomicInteger(0);
        ConcurrentHashMap<Object, ListenableFuture<?>> context = new ConcurrentHashMap<>();

        Callable<Boolean> ms10Callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(10);
                innerCheck.incrementAndGet();
                return Boolean.TRUE;
            }
        };
        ListenableFuture<Boolean> firstFuture = Futures2.transformWithGroupingByTag(unlimitedExec, context, "someTag",
                ms10Callable);

        ListenableFuture<Boolean> secondFuture = Futures2.transformWithGroupingByTag(unlimitedExec, context, "someTag",
                ms10Callable);
        assertEquals(1, context.size());

        assertEquals(Arrays.asList(Boolean.TRUE, Boolean.TRUE),
                Futures.<Boolean>allAsList(firstFuture, secondFuture).get());
        assertEquals(1, innerCheck.get());
        Thread.sleep(5);
        assertEquals(0, context.size());
    }

    private void createUnlimitedExec() {
        unlimitedExec = MoreExecutors.listeningDecorator(
                new ThreadPoolExecutor(1, Integer.MAX_VALUE, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
    }
}
