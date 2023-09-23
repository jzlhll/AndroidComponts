package com.au.module_flyablejson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标记一个变量。设定需要映射的后台字段。
 * 比如后台返回的是class，端代码写的是clazz。
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface TransformFlyable {
    public String value();
}