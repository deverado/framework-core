package de.deverado.framework.core.propertyconfig;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.deverado.framework.core.Collections3;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropsConfigurator {

    public static final String PROPS_CONFIG_ENV_ATTR = "props_config_env";
    private static final Logger log = LoggerFactory
            .getLogger(PropsConfigurator.class);
    private final Properties defaults;
    private final boolean checkInitialContext;
    private String specifier = "";
    private List<PropsConfiguratorPlugin> plugins = new ArrayList<>();

    public PropsConfigurator(@javax.annotation.Nullable Properties defaults,
            boolean checkInitialContext, PropsConfiguratorPlugin... pluginsParam) {
        this.checkInitialContext = checkInitialContext;
        if (defaults != null) {
            this.defaults = defaults;
        } else {
            this.defaults = new Properties();
        }
        if (pluginsParam != null) {
            this.plugins.addAll(Collections3.filterNullsAndShrinkMaybe(pluginsParam, false));
        }
    }

    /**
     * If specified looking for: app.specifier.ENV.properties.
     *
     */
    public PropsConfigurator setSpecifier(String specifier) {
        this.specifier = Strings.nullToEmpty(specifier);
        return this;
    }

    public String getSpecifier() {
        return specifier;
    }

    /**
     * This loads app.properties and app.prod properties. app.prod properties
     * override app.properties, but app.properties provides defaults.
     * <p>
     * Finally it loads System.properties as upper layer.
     *
     */
    public Properties loadPropsCombination() {
        String ctxtEnvEntry = tryGetFromInitialContext();
        String envSysProp = System.getProperty(PROPS_CONFIG_ENV_ATTR);
        String puppetEnvProp = tryGetFromPuppetEnvSetting();
        String envProp = null;
        if (!StringUtils.isBlank(puppetEnvProp)) {
            envProp = puppetEnvProp;
            if ((!StringUtils.isBlank(envSysProp))
                    || (!StringUtils.isBlank(ctxtEnvEntry))) {
                log.warn(
                        "Puppet environment overriding {} Systemprop {} and context entry {}",
                        PROPS_CONFIG_ENV_ATTR, envSysProp, ctxtEnvEntry);
            }
        } else {
            if ((!Strings.isNullOrEmpty(ctxtEnvEntry))
                    && !Strings.isNullOrEmpty(envSysProp)) {
                log.warn("Both InitialContext env-entry and system property are set. Taking env entry: "
                        + ctxtEnvEntry + "(sysp:" + envSysProp + ")");
                envProp = ctxtEnvEntry;
            } else if (!Strings.isNullOrEmpty(ctxtEnvEntry)) {
                envProp = ctxtEnvEntry;
            } else if (!Strings.isNullOrEmpty(envSysProp)) {
                envProp = envSysProp;
            }
        }
        return loadPropsCombinationWithGivenEnvOnly(envProp);
    }

    /**
     * 
     * @param envProp
     *            if <code>null</code> or empty looks in system props etc for
     *            environment. Else the given prop is used.
     */
    public Properties loadPropsCombination(String envProp) {
        if (StringUtils.isBlank(envProp)) {
            return loadPropsCombination();
        } else {
            return loadPropsCombinationWithGivenEnvOnly(envProp);
        }
    }

    public Properties loadPropsCombinationWithGivenEnvOnly(String envProp) {
        Properties retval = addPropsResourceIfAvailable(defaults,
                getPropsFilename(null));

        if (envProp != null) {
            envProp = envProp.trim();
            if (!envProp.matches("\\w+")) {
                throw new RuntimeException(
                        "Illegal value for puppet_environment or "
                                + PROPS_CONFIG_ENV_ATTR
                                + "='"
                                + envProp
                                + "'! "
                                + "Only word-chars allowed. Empty string not allowed.");
            }
            retval = addPropsResourceIfAvailable(retval,
                    getPropsFilename(envProp));
            // ensure environment is properly set
            retval.setProperty("app.env", envProp);
        }

        retval = addSystemEnvironment(retval);
        retval = addSystemProperties(retval);

        return loadPropsFromParameterOnly(retval);
    }

    /**
     * Currently only resolves functions and dumps if required. Useful for testing function plugins.
     *
     */
    public Properties loadPropsFromParameterOnly(Properties toLoad) {
        toLoad = resolveFunctions(toLoad);

        debugDump(toLoad);

        return toLoad;
    }

    private Properties resolveFunctions(Properties param) {
        Properties newProps = new Properties(param);

        for (String k : param.stringPropertyNames()) {
            String val = param.getProperty(k);
            if (val.startsWith("$") || val.startsWith("#")) {
                newProps.setProperty(k, resolveFunction(param, k, val));
            }
        }
        return newProps;
    }

    protected static Pattern FUNC_NAME_PATTERN = Pattern
            .compile("^\\s*[$#]([^,\\(\\s\\)]+)");
    protected static Pattern FUNC_PARAM_PATTERN = Pattern
            .compile("\\(?,?\\s*([^,\\(\\)\\s]+)");

    protected String resolveFunction(Properties config, String key, String val) {
        Matcher nameMatcher = FUNC_NAME_PATTERN.matcher(val);
        if (nameMatcher.find()) {
            String funcName = nameMatcher.group(1);
            List<String> params = getParams(val.subSequence(nameMatcher.end(),
                    val.length()));

            boolean knownFunc = false;
            for (PropsConfiguratorPlugin p : plugins) {
                if (p.isProvidingFunction(funcName)) {
                    knownFunc = true;
                    val = p.executeFunction(funcName, config, key, val, params);
                    break;
                }
            }
            if (!knownFunc) {
                log.warn("Unknown function: {}", val);
            }
        } else {
            log.warn("Unrecognized function expression: {}:{}", key, val);
        }
        return val;
    }



    protected List<String> getParams(CharSequence subSequence) {
        List<String> retval = Lists.newArrayList();
        Matcher matcher = FUNC_PARAM_PATTERN.matcher(subSequence);
        while (matcher.find()) {
            retval.add(matcher.group(1));
        }
        return retval;
    }

    protected String getPropsFilename(@Nullable String envProp) {
        StringBuilder sb = new StringBuilder("app");
        if (!Strings.isNullOrEmpty(getSpecifier())) {
            sb.append(".").append(getSpecifier());
        }
        if (!Strings.isNullOrEmpty(envProp)) {
            sb.append(".").append(envProp);
        }
        sb.append(".properties");
        return sb.toString();
    }

    protected void debugDump(Properties retval) {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();

            Set<String> propertyNames = Sets.newTreeSet(retval
                    .stringPropertyNames());
            for (String name : propertyNames) {
                String prop = retval.getProperty(name);
                if (name.contains("pass")
                        || name.contains("pw")) {
                    sb.append(name + "=<hidden, name contains pw or pass>")
                            .append("\n");
                } else {
                    sb.append(name + "=" + prop).append("\n");
                }
            }
            log.debug("Properties: \n{}", sb.toString());
        }
    }

    private Properties addSystemEnvironment(Properties param) {
        Properties newProps = new Properties(param);

        for (Entry<String, String> e : System.getenv().entrySet()) {
            newProps.setProperty(e.getKey(), e.getValue());
        }
        return newProps;
    }

    private Properties addSystemProperties(Properties param) {
        Properties newProps = new Properties(param);
        Enumeration<?> sysNames = System.getProperties().propertyNames();
        while (sysNames.hasMoreElements()) {
            String nextName = sysNames.nextElement().toString();
            newProps.setProperty(nextName, System.getProperty(nextName));
        }
        return newProps;
    }

    private String tryGetFromInitialContext() {
        if (checkInitialContext) {
            try {
                Context ctxt = (Context) new InitialContext()
                        .lookup("java:comp/env");
                return (String) ctxt.lookup(PROPS_CONFIG_ENV_ATTR);
            } catch (NamingException e) {
                log.debug("Cannot load env value from servlet env-entry");
            }
        }
        return null;
    }

    private String tryGetFromPuppetEnvSetting() {
        String puppetEnv = System.getenv("puppet_environment");

        log.debug("Got puppet_environment env value: {}", puppetEnv);

        return puppetEnv;
    }

    private Properties addPropsResourceIfAvailable(Properties defaults,
            String resName) {
        Properties retval = defaults;
        InputStream props = PropsConfigurator.class.getClassLoader()
                .getResourceAsStream(resName);
        try {
            if (props != null) {
                log.info("Found properties file '{}', adding it.", resName);
                retval = new Properties(retval);
                try {
                    retval.load(props);
                } catch (IOException ioe) {
                    retval = null; // avoid eating it in finally
                    throw new RuntimeException(
                            "Configuration could not be loaded", ioe);
                }
            } else {
                log.warn("Expecting properties file '" + resName
                        + "' but not found in classpath.");
            }
        } finally {
            if (props != null) {
                try {
                    props.close();
                } catch (IOException ioe) {
                    log.error("Closing loaded props failed: ", ioe);
                }
            }
        }
        return retval;
    }
}
