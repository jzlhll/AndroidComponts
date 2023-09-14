package com.au.module_flyablejson.apt.types;

/**
 * 直接泛参数。
 * 因为FieldType和子类，是描述一条变量。
 * 那么本类，描述的是泛型变量，

 Bean {
    public T a; //描述的是这一条。
 }

 */
public class GenericFieldType extends FieldType{
    public GenericFieldType(String fieldName, String setFunctionName) {
        super(Object.class, fieldName, setFunctionName);
    }

    public GenericFieldType(String fieldName) {
        super(Object.class, fieldName);
    }
}
