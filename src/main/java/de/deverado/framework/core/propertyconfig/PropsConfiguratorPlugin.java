package de.deverado.framework.core.propertyconfig;/*
 * Copyright Georg Koester 2012-2015. All rights reserved.
 */

import java.util.List;
import java.util.Properties;

public interface PropsConfiguratorPlugin {

    public static final Object[] FUNCTION_PARAMS = {Properties.class, String.class, String.class, List.class};

    public boolean isProvidingFunction(String name);

    public String executeFunction(String name, Properties config, String key, String val, List<String> params)
            throws RuntimeException;

    public interface PropsConfiguratorFunction {
        public String call(Properties config, String key, String val, List<String> params);
    }
}
