package de.deverado.framework.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class FileTransformerTest {

    private FileTransformer t;

    @Before
    public void setUp() throws Exception {
        t = new FileTransformer();

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldWorkDifferentDings() throws Exception {
        File src = File.createTempFile("fileTransformer1src", "tmp");
        src.deleteOnExit();
        Files.write("src", src, Charsets.UTF_8);
        File dst = File.createTempFile("fileTransformer2dst", "tmp");
        dst.deleteOnExit();
        Files.write("dst", dst, Charsets.UTF_8);

        t.setSrc(src);
        t.setTarget(dst);

        final AtomicReference<File> innerTarget = new AtomicReference<File>();
        t.modify(new FileTransformation() {

            @Override
            public void transform(File src, File target) throws Exception {
                assertEquals("src", Files.toString(src, Charsets.UTF_8));
                // target should be the empty tmp file
                assertEquals("dst", Files.toString(target, Charsets.UTF_8));
                innerTarget.set(target);

                Files.append("mod", target, Charsets.UTF_8);
            }
        });
        assertTrue(innerTarget.get().exists());
        assertEquals(innerTarget.get(), dst);
        assertEquals("dstmod", Files.toString(dst, Charsets.UTF_8));
        t.finishedSuccessfully();

        assertEquals("dstmod", Files.toString(dst, Charsets.UTF_8));
    }

    @Test
    public void shouldWorkSameDings() throws Exception {
        File src = File.createTempFile("fileTransformer1src", "tmp");
        src.deleteOnExit();
        Files.write("src", src, Charsets.UTF_8);

        t.setSrc(src);

        final AtomicReference<File> innerTarget = new AtomicReference<File>();
        t.modify(new FileTransformation() {

            @Override
            public void transform(File src, File target) throws Exception {
                assertEquals("src", Files.toString(src, Charsets.UTF_8));
                // target should be the empty tmp file
                assertEquals("", Files.toString(target, Charsets.UTF_8));
                innerTarget.set(target);

                Files.append("mod", target, Charsets.UTF_8);
            }
        });
        assertTrue(innerTarget.get().exists());
        assertFalse(innerTarget.get().equals(src));
        assertEquals("src", Files.toString(src, Charsets.UTF_8));
        t.finishedSuccessfully();

        assertEquals("mod", Files.toString(src, Charsets.UTF_8));
        assertFalse(innerTarget.get().exists());
    }

    @Test
    public void shouldNotCleanupIfSameDings() throws Exception {
        File src = File.createTempFile("fileTransformer1src", "tmp");
        src.deleteOnExit();
        Files.write("src", src, Charsets.UTF_8);
        File dst = File.createTempFile("fileTransformer2dst", "tmp");
        dst.deleteOnExit();
        Files.write("dst", dst, Charsets.UTF_8);

        t.setSrc(src);
        t.setTarget(dst);

        final AtomicReference<File> innerTarget = new AtomicReference<File>();
        t.modify(new FileTransformation() {

            @Override
            public void transform(File src, File target) throws Exception {
                assertEquals("src", Files.toString(src, Charsets.UTF_8));
                // target should be the empty tmp file
                assertEquals("dst", Files.toString(target, Charsets.UTF_8));
                innerTarget.set(target);

                Files.append("mod", target, Charsets.UTF_8);
            }
        });
        t.close();
        assertTrue(innerTarget.get().exists());
        assertEquals(innerTarget.get(), dst);
        assertEquals("dstmod", Files.toString(dst, Charsets.UTF_8));

    }

    @Test
    public void shouldCleanupOnProblemWithSameDings() throws Exception {
        File src = File.createTempFile("fileTransformer1src", "tmp");
        src.deleteOnExit();
        Files.write("src", src, Charsets.UTF_8);

        t.setSrc(src);

        final AtomicReference<File> innerTarget = new AtomicReference<File>();
        t.modify(new FileTransformation() {

            @Override
            public void transform(File src, File target) throws Exception {
                assertEquals("src", Files.toString(src, Charsets.UTF_8));
                // target should be the empty tmp file
                assertEquals("", Files.toString(target, Charsets.UTF_8));
                innerTarget.set(target);

                Files.append("mod", target, Charsets.UTF_8);
            }
        });
        t.close();
        assertFalse(innerTarget.get().equals(src));
        assertFalse(innerTarget.get().exists());
        assertEquals("src", Files.toString(src, Charsets.UTF_8));

    }
}
