package de.deverado.framework.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExceptionsHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldWrapException() {
        try {
            if ("89341234".contains("1")) {
                throw new IllegalArgumentException("some msg");
            }
        } catch (Exception e) {
            RuntimeException newWithCause = ExceptionsHelper.newWithCause(
                    RuntimeException.class, e, null);
            assertNotNull(newWithCause);
            assertEquals(IllegalArgumentException.class.getName()
                    + ": some msg", newWithCause.getMessage());
            assertEquals(e, newWithCause.getCause());
        }
    }

    @Test
    public void shouldWrapExceptionWithNewMessage() {
        try {
            if ("89341234".contains("1")) {
                throw new IllegalArgumentException("some msg");
            }
        } catch (Exception e) {
            RuntimeException newWithCause = ExceptionsHelper.newWithCause(
                    RuntimeException.class, e, "outer msg");
            assertNotNull(newWithCause);
            assertEquals("outer msg", newWithCause.getMessage());
            assertEquals(e, newWithCause.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldOrderConstructorsCorrectly() {
        List<Constructor<RuntimeException>> list = ExceptionsHelper
                .preferringStringsWithThrowables(Arrays
                        .asList((Constructor<RuntimeException>[]) RuntimeException.class
                                .getConstructors()));
        assertTrue(list
                .toString()
                .startsWith(
                        "[public java.lang.RuntimeException(java.lang.String,java.lang.Throwable)"));
    }
}
