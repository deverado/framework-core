package de.deverado.framework.core;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntervalCounterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldWork() throws InterruptedException {
        IntervalCounter counter = new IntervalCounter(10, TimeUnit.MILLISECONDS);
        assertEquals(0, counter.get());
        assertEquals(1, counter.incrementAndGet());
        assertEquals(2, counter.incrementAndGet());
        assertEquals(2, counter.get());
        Thread.sleep(20);
        assertEquals(0, counter.get());
        assertEquals(1, counter.incrementAndGet());
        Thread.sleep(20);
        assertEquals(11, counter.incrementAndGet(11));
        assertEquals(11, counter.get());
    }

}
