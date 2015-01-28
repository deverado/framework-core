package de.deverado.framework.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Numbers2Test {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldConvertToBinString() {
        assertEquals("00000000000000000000000000000001",
                Numbers2.toBinaryString(1));
        assertEquals("01111111111111111111111111111111",
                Numbers2.toBinaryString(Integer.MAX_VALUE));
        assertEquals("10000000000000000000000000000000",
                Numbers2.toBinaryString(Integer.MIN_VALUE));

    }

    @Test
    public void shouldConvertLongToBinString() {
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000001",
                Numbers2.toBinaryString(1L));
        assertEquals(
                "0111111111111111111111111111111111111111111111111111111111111111",
                Numbers2.toBinaryString(Long.MAX_VALUE));
        assertEquals(
                "1000000000000000000000000000000000000000000000000000000000000000",
                Numbers2.toBinaryString(Long.MIN_VALUE));

    }

}
