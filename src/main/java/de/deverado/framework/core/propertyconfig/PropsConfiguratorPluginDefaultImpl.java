package de.deverado.framework.core.propertyconfig;/*
 * Copyright Georg Koester 2012-15. All rights reserved.
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Use one of the static create methods, extending doesn't work.
 */
public class PropsConfiguratorPluginDefaultImpl implements PropsConfiguratorPlugin {

    private final Map<String, Method> methodMap;
    private final Object pluginInstance;

    public PropsConfiguratorPluginDefaultImpl(Object pluginInstance, Map<String, Method> map) {
        this.methodMap = map;
        this.pluginInstance = pluginInstance;
    }

    public PropsConfiguratorPluginDefaultImpl(Object pluginInstance) {
        this(pluginInstance, createMethodMap(pluginInstance, getTargetMethods(pluginInstance)));
    }

    public PropsConfiguratorPluginDefaultImpl create(Object pluginInstance) {
        return new PropsConfiguratorPluginDefaultImpl(pluginInstance);
    }

    /**
     * Checks if there is at least one target method, if none is found null is returned.
     */
    public static PropsConfiguratorPluginDefaultImpl createIfIsCompatiblePlugin(Object possiblePlugin) {
        List<Method> methodsWithAnnotation = getTargetMethods(possiblePlugin);
        if (methodsWithAnnotation.isEmpty()) {
            return null;
        }
        return create(possiblePlugin, methodsWithAnnotation);
    }

    public static PropsConfiguratorPluginDefaultImpl create(final Object pluginInstance,
                                                            List<Method> methodsWithAnnotation) {

        Map<String, Method> map = createMethodMap(pluginInstance, methodsWithAnnotation);
        return new PropsConfiguratorPluginDefaultImpl(pluginInstance, map);
    }

    @Override
    public boolean isProvidingFunction(String name) {
        return methodMap.containsKey(name);
    }

    @Override
    public String executeFunction(String funcName, Properties config, String key, String val, List<String> params)
            throws RuntimeException {

        Method m = methodMap.get(funcName);
        if (m == null) {
            throw new RuntimeException("Function unknown, check with isProvidingFunction before calling " +
                    "executeFunction!");
        }
        return invokePluginFunc(pluginInstance, m, config, key, val, params);
    }

    private static List<Method> getTargetMethods(Object possiblePlugin) {
        return getMethodsWithAnnotation(possiblePlugin.getClass(),
                PropsConfiguratorPluginFuncAnnotation.class);
    }

    private static Map<String, Method> createMethodMap(Object pluginInstance, List<Method> methodsWithAnnotation) {
        Map<String, Method> map = new HashMap<>();
        for (Method m : methodsWithAnnotation) {
            if (!Arrays.equals(m.getParameterTypes(), FUNCTION_PARAMS)) {
                throw new RuntimeException("Method parameters on " + pluginInstance.getClass() + "." + m.getName() +
                        " invalid, need: " + Arrays.asList(FUNCTION_PARAMS));
            }
            if (null != map.put(m.getName(), m)) {
                throw new RuntimeException(//
                        String.format("Duplicate plugin method %s in %s, overloading not permitted.", //
                                m.getName(), pluginInstance.getClass()));
            }
        }
        return map;
    }

    private String invokePluginFunc(Object pluginInstance, Method m, Properties config, String key, String val,
                                    List<String> params) {
        try {
            return (String) m.invoke(pluginInstance, new Object[]{config, key, val, params});
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke PropsConfiguratorPlugin method on " + pluginInstance, e);
        }
    }

    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Method> retval = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getAnnotation(annotationClass) != null) {
                retval.add(m);
            }
        }
        return retval;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + pluginInstance;
    }
}
