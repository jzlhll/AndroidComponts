package com.au.module_flyablejson.apt.types;

import java.util.Arrays;

/**
 * 本类描述的是：
 * 比如List<T>,  ApiBean<T1, T2, ...>的形式。
 * 是带泛型参数类型的类型。
 */
public class ParameterizedGenericFieldType extends ParameterizedFieldType {
    /**
     * 创建一条私有field。参数化类型。
     */
    public ParameterizedGenericFieldType(Class<?> type, int paramNum, String fieldName, String setFunctionName) {
        super(type, createObjectClasses(paramNum), fieldName, setFunctionName);
    }

    /**
     * 创建一条公开field.参数化类型。
     */
    public ParameterizedGenericFieldType(Class<?> type, int paramNum, String fieldName) {
        super(type, createObjectClasses(paramNum), fieldName);
    }

    private static Class<?>[] createObjectClasses(int num) {
        Class<?>[] classes = new Class[num];
        Arrays.fill(classes, Object.class);
        return classes;
    }
}