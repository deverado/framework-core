package de.deverado.framework.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Base64ExtTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldMatchBase64UrlSafeCorrectly() {
        assertTrue(Base64Ext.isBase64UrlSafeCompliantString(""));
        assertTrue(Base64Ext.isBase64UrlSafeCompliantString("asf23490-_"));
        assertFalse(Base64Ext.isBase64UrlSafeCompliantString("as/f23490-_"));
        assertFalse(Base64Ext.isBase64UrlSafeCompliantString("asf23490-_/"));
        assertFalse(Base64Ext.isBase64UrlSafeCompliantString("asf23490-_."));
    }

}
