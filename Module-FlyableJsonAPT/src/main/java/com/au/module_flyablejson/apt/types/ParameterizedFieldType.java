package com.au.module_flyablejson.apt.types;

/**
 * 比如List<String>, List<InfoBean>, ApiBean<InfoSubBean>的形式。
 * 比如ApiBean<InfoSubBean1, InfoSubBean2, ...>的形式。
 *
 * 是带具体明确参数类型的类型。
 */
public class ParameterizedFieldType extends FieldType {
    /**
     * 创建一条私有field。参数化类型。
     */
    public ParameterizedFieldType(Class<?> type, Class<?>[] parameterizedTypes, String fieldName, String setFunctionName) {
        super(type, fieldName, setFunctionName);
        this.parameterizedTypes = parameterizedTypes;
    }

    /**
     * 创建一条公开field.参数化类型。
     */
    public ParameterizedFieldType(Class<?> type, Class<?>[] parameterizedTypes, String fieldName) {
        super(type, fieldName);
        this.parameterizedTypes = parameterizedTypes;
    }

    final Class<?>[] parameterizedTypes;

    public Class<?>[] getParameterizedType() {
        return parameterizedTypes;
    }
}