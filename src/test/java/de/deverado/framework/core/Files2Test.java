package de.deverado.framework.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Files2Test {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSplitPath() {

        assertArrayEquals(new String[] {}, Files2.splitPath(""));
        assertArrayEquals(new String[] {}, Files2.splitPath((File) null));
        assertArrayEquals(new String[] { "asdf" }, Files2.splitPath("asdf"));
        assertArrayEquals(new String[] { "asdf", "klkl" },
                Files2.splitPath("asdf/klkl"));
        assertArrayEquals(new String[] { "asdf", "klkl" },
                Files2.splitPath("/asdf/klkl"));
        assertArrayEquals(new String[] { "asdf", "klkl" },
                Files2.splitPath("asdf//klkl"));
        assertArrayEquals(new String[] { "asdf", "klkl" },
                Files2.splitPath("asdf/klkl/"));
        assertArrayEquals(new String[] { "asdf", " ", "klkl" },
                Files2.splitPath("asdf/ /klkl/"));
    }

    @Test
    public void shouldJoinPaths() {
        assertEquals("aa/bb/ds",
                Files2.joinPath("aa", new String[] { "bb", "ds" }));
        assertEquals("aa/bb/ds",
                Files2.joinPath("aa", "bb", new String[] { "ds" }));
        assertEquals("aa/bb/ds",
                Files2.joinPath("aa", new String[] { "bb", "ds" }, 0, 2));

        assertEquals("aa/ds",
                Files2.joinPath("aa", new String[] { "bb", "ds" }, 1, 2));

        assertEquals("aa/ds", Files2.joinPath("aa", new String[] { "", "ds" }));
    }

}
