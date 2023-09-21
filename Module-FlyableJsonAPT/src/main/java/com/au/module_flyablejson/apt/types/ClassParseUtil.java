package com.au.module_flyablejson.apt.types;

import org.jetbrains.annotations.Nullable;

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

    private static FieldType createField(Field field, @Nullable Method[] methods) {
        if (field.isSynthetic()) { //todo: 目前只过滤这一种不做解析
            return null;
        }

        var name = field.getName();
        var isStartWithIs = name.startsWith("is");
        var expectMethodName1 = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        var expectMethodName2 = isStartWithIs ? "set" + Character.toUpperCase(name.charAt(1)) + name.substring(2) : null;
        //共计处理两种case
        //1.
        //String normal;
        // setNormal() {}

        //2.
        //boolean isUsed;
        // setIsUsed() {}  setUsed() {}

        String foundMethod = null;
        if (methods != null) {
            for () {

            }
        }
    }
}
