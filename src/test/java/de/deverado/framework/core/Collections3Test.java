package de.deverado.framework.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class Collections3Test {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void flattenShouldFlattenArrays() {
        assertArrayEquals(new String[]{"a", "b", null, "c", "4"}, Collections3.flatten(String.class, new String[][]
                {new String[]{"a"}, null, new String[]{"b", null, "c"}, null, null, new String[]{"4"}}));

        assertArrayEquals(new String[]{}, Collections3.flatten(String.class, (String[][])null));

    }

    @Test
    public void filterNullsAndShrinkShouldFilterCorrectly() {
        assertArrayEquals(new String[]{"a", "b", "4"}, Collections3.filterNullsAndShrinkMaybe(
                new String[]{"a", "b", null, "4", null, null}, true).toArray(new String[3]));

        assertTrue(Collections3.filterNullsAndShrinkMaybe((String[])null, true).isEmpty());

    }

}
