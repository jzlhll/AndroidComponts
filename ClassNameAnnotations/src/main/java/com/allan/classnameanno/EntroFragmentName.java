package com.allan.classnameanno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author allan
 * @date :2024/6/20 14:34
 * @description:
 */
@Retention(RetentionPolicy.CLASS)
@Target(value = ElementType.TYPE)
public @interface EntroFragmentName {
}