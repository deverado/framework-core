package de.deverado.framework.core.propertyconfig;/*
 * Copyright Georg Koester 2012-15. All rights reserved.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PropsConfiguratorPluginFuncAnnotation {
}
