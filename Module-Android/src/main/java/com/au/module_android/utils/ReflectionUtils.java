package com.au.module_android.utils;

import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author au
 * desc: 用于反射调用代码
 */
public final class ReflectionUtils {
    public static Object createInstance(String fullName, Object... objs) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = getProtectedClass(fullName);
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> first = constructors[0];

        return first.newInstance(objs);
    }

    /**
     * 获取被保护的class。比如package protected
     */
    public static Class<?> getProtectedClass(String fullName) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.loadClass(fullName);
    }

    public static void setFinalStaticFieldNewValue(Class<?> clazz, String name, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(name);
        setFinalStaticNewValue(field, newValue, true);
    }

    /**
     * final变量进行调整
     */
    public static void setFinalStaticNewValue(Field field, Object newValue, boolean resetFinal) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);

        if (resetFinal) {
            modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
        }
    }

    public static Object getStaticPrivateFieldValue(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(clazz);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取私有成员变量的值
     */
    public static Object getPrivateFieldValue(Object instance, String filedName) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(filedName);
        field.setAccessible(true);
        return field.get(instance);
    }

    /**
     * 一直往父类获取私有成员变量的值
     * @param ignoreSelf true则直接查找父类。这样的话，我们就可以取同名了。
     */
    public static Object iteratorGetPrivateFieldValue(Object instance, String fieldName, boolean ignoreSelf) {
        for (Class<?> superClass = ignoreSelf ? instance.getClass().getSuperclass() : instance.getClass();
             superClass != null && superClass != Object.class;
             superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }
        }

        return null;
    }

    /**
     * 设置私有成员的值
     *
     * @param instance
     * @param fieldName
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void setPrivateFieldNewValue(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    public static void setPrivateFinalFieldNewValue(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(instance, value);
    }

    /**
     * 访问私有方法
     *
     * @param instance
     * @param methodName
     * @param parameterTypes
     * @param vals
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invokePrivateMethod(Object instance, String methodName, Class<?>[] parameterTypes, Object[] vals) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, vals);
    }

    public static Method getPrivateMethod(Object instance, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    public static Method iteratorGetPrivateMethod(Object instance, String methodName, Class<?>... parameterTypes) {
        for (Class<?> superClass = instance.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Method m = superClass.getDeclaredMethod(methodName, parameterTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                //Method 不在当前类定义, 继续向上转型 不能
            }
        }

        return null;
    }
}
