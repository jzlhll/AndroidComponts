package com.au.module_flyablejson.apt;

import com.au.module_flyablejson.apt.types.FieldType;

import java.util.HashMap;
import java.util.List;

public class JavaCodeGenerator {
    String clazz = """
            package $package;
            
            """;

    //all fields: classFullName -> list{name, type, }
    private static final beanFieldsMap = new HashMap<String, List<FieldType>>()
}
