package com.au.module_flyablejson.apt.types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ClassParseUtil {
    public static ClassType parse(Class<?> beanClass) {
        ClassType ct = new ClassType();
        ct.packageName = beanClass.getPackageName();
        ct.className = beanClass.getSimpleName();
        ct.fullName = beanClass.getName();

        ct.fieldTypes = new ArrayList<>();

        var fields = beanClass.getFields();
        var methods = beanClass.getDeclaredMethods();

        for (var field : fields) {
            var ft = createField(field, methods);
            if (ft != null) {
                ct.fieldTypes.add(ft);
            }
        }
    }

    private static FieldType createField(Field field, Method[] methods) {
        if (field.isSynthetic()) { //todo: 目前只过滤这一种不做解析
            return null;
        }

        if (field.) {

        }

    }
}
