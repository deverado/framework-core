package de.deverado.framework.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParsingUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParseBoolean() {
        assertTrue(ParsingUtil.parseAsBoolean("true"));
        assertTrue(ParsingUtil.parseAsBoolean("tRue"));
        assertTrue(ParsingUtil.parseAsBoolean("1"));
        assertTrue(ParsingUtil.parseAsBoolean("100"));
        assertTrue(ParsingUtil.parseAsBoolean("yes"));

        assertFalse(ParsingUtil.parseAsBoolean(""));
        assertFalse(ParsingUtil.parseAsBoolean(null));
        assertFalse(ParsingUtil.parseAsBoolean("false"));
        assertFalse(ParsingUtil.parseAsBoolean("-1"));
        assertFalse(ParsingUtil.parseAsBoolean("1.0"));
    }

}
