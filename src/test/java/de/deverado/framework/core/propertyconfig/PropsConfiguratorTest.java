package de.deverado.framework.core.propertyconfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropsConfiguratorTest {

    private PropsConfigurator pc;
    private Properties props;

    @Before
    public void setUp() throws Exception {
        pc = new PropsConfigurator(null, false, TestPropsConfiguratorPlugin.create());
        props = new Properties();
        props.put("aKey", "aVal");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldMatchFuncName() {
        Matcher matcher = PropsConfigurator.FUNC_NAME_PATTERN.matcher("$blub");
        assertTrue(matcher.find());
        assertEquals("blub", matcher.group(1));

        matcher = PropsConfigurator.FUNC_NAME_PATTERN.matcher("#blub");
        assertTrue(matcher.find());

        matcher = PropsConfigurator.FUNC_NAME_PATTERN
                .matcher("$blub(fdsa, fdkl)");
        assertTrue(matcher.find());
        assertEquals("blub", matcher.group(1));

        matcher = PropsConfigurator.FUNC_NAME_PATTERN
                .matcher("   $bl_+ub(fdsa, fdkl)");
        assertTrue(matcher.find());
        assertEquals("bl_+ub", matcher.group(1));

    }

    @Test
    public void shouldParseArgs() {
        List<String> params = pc.getParams("(asdf)");
        assertEquals(1, params.size());
        assertEquals("asdf", params.get(0));

        params = pc.getParams("()");
        assertEquals(0, params.size());
        params = pc.getParams("");
        assertEquals(0, params.size());

        params = pc.getParams("(asdf,fds)");
        assertEquals(2, params.size());
        assertEquals("asdf", params.get(0));
        assertEquals("fds", params.get(1));

        params = pc.getParams("  ( asdf  , fds  ,asd )  ");
        assertEquals(3, params.size());
        assertEquals("asdf", params.get(0));
        assertEquals("fds", params.get(1));
        assertEquals("asd", params.get(2));
    }

    @Test
    public void shouldCorrectlyResolveFunctions() {
        assertEquals("k[p, p2]", pc.resolveFunction(props, "k", "$concatKeyParams(p, p2)"));
        assertEquals("k[]", pc.resolveFunction(props, "k", "$concatKeyParams()"));
    }

    private static class TestPropsConfiguratorPlugin {
        public static PropsConfiguratorPlugin create() {
            return new PropsConfiguratorPluginDefaultImpl(new TestPropsConfiguratorPlugin());
        }

        @PropsConfiguratorPluginFuncAnnotation
        public String concatKeyParams(Properties config, String key, String val, List<String> params) {
            return "" + key + params;
        }
    }
}
