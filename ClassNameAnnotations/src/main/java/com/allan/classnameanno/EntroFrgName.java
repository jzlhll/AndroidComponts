package com.allan.classnameanno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author allan
Retention RetentionPolicy：
SOURCE：使用该标明的注解将在编译阶段就被抛弃掉。
CLASS： 使用该标明的注解将在编译阶段记录到生成的class文件中，但在运行阶段时又会被VM抛弃。默认是该模式。
RUNTIME：使用该标明的注解将在编译阶段被保存在生成的class文件中，同时在运行阶段时会保存到VM中。所以它该注解将一直存在，自然能够通过java的反射机制进行读取。

Target：ElementType：
TYPE: 作用于类、接口或者枚举
FIELD：作用于类中声明的字段或者枚举中的常量
METHOD：作用于方法的声明语句中
PARAMETER：作用于参数声明语句中
CONSTRUCTOR：作用于构造函数的声明语句中
LOCAL_VARIABLE：作用于局部变量的声明语句中
ANNOTATION_TYPE：作用于注解的声明语句中
PACKAGE：作用于包的声明语句中
TYPE_PARAMETER：java 1.8之后，作用于类型声明的语句中
TYPE_USE：java 1.8之后，作用于使用类型的任意语句中
 */
@Retention(RetentionPolicy.CLASS)
@Target(value = ElementType.TYPE)
public @interface EntroFrgName {
    int priority() default 0;
    String customName() default "";
}