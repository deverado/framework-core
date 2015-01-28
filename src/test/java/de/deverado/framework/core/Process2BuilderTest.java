package de.deverado.framework.core;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class Process2BuilderTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldWork() throws Exception {
        Process2Builder builder = new Process2Builder() {
            @Override
            protected ListeningExecutorService getDefaultExecutor() {
                return MoreExecutors.listeningDecorator(Executors
                        .newCachedThreadPool());
            }
        };

        Process process = new ProcessBuilder("echo", "hallo").start();
        Process2 process2 = builder.build(process);

        assertEquals("done", process2.getInputReaderFuture().get());
        assertEquals(
                "hallo\n",
                new String(((ByteArrayOutputStream) process2
                        .getProcessOutputTarget()).toByteArray()));
        assertEquals(new Integer(0), process2.getProcessExitvalFuture().get());

    }

}
