package com.au.module_flyablejson.apt.types;

/**
 * 描述一条成员变量。

 比如

 Bean {
     public String a; //描述的是这一条。

     private SubBean b; //或者描述这一条+他的set函数名。
     public getB() {

     }
 }

 */
public class FieldType {
    /**
     * 创建一条私有field
     */
    public FieldType(Class<?> type, String fieldName, String setFunctionName) {
        this.isPublic = false;
        this.type = type;
        this.fieldName = fieldName;
        this.setFunctionName = setFunctionName;
    }

    /**
     * 创建一条公开field
     */
    public FieldType(Class<?> type, String fieldName) {
        this.isPublic = true;
        this.type = type;
        this.fieldName = fieldName;
        this.setFunctionName = null;
    }

    final String setFunctionName;

    final Class<?> type;
    final String fieldName;
    final boolean isPublic;

    public Class<?> getType() {
        return type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isPublic() {
        return isPublic;
    }
    public String getSetFunctionName() {
        return setFunctionName;
    }
}
