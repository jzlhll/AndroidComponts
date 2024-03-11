package com.au.aulitesql.util;

import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author au
 * @date :2023/11/15 15:08
 * @description:
 */
public final class GsonUtil {
    public static <E> List<E> gsonFromList(@NonNull String json, Class<E> elementClass) {
        return gsonFromList(json, AuLiteSql.getGsonOrNew(), elementClass);
    }

    public static <K,V> Map<K,V> gsonFromMap(@NonNull String json, Class<K> keyClass, Class<V> valClass) {
        return gsonFromMap(json, AuLiteSql.getGsonOrNew(), keyClass, valClass);
    }

    public static <E> List<E> gsonFromList(@NonNull String json, @NonNull Gson gson, Class<E> elementClass) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        var listType = TypeToken.getParameterized(ArrayList.class, elementClass).getType();
        return gson.fromJson(json, listType);
    }

    public static <K,V> Map<K,V> gsonFromMap(@NonNull String json, @NonNull Gson gson, Class<K> keyClass, Class<V> valClass) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        var listType = TypeToken.getParameterized(HashMap.class, keyClass, valClass).getType();
        return gson.fromJson(json, listType);
    }

    public static <K,V> Map<K,V> gsonFromGeneric(@NonNull String json, @NonNull Field field) {
        return gsonFromGeneric(json, AuLiteSql.getGsonOrNew(), field);
    }

    public static <K,V> Map<K,V> gsonFromGeneric(@NonNull String json, @NonNull Gson gson, @NonNull Field field) {
        var genericType = field.getGenericType();
        var rawType = field.getType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            return gsonFromGeneric(json, gson, rawType, parameterizedType.getActualTypeArguments());
        }
        throw new IllegalArgumentException("gsonFromGeneric genericType is bad: " + genericType + " field " + field);
    }

    public static <K,V> Map<K,V> gsonFromGeneric(@NonNull String json, @NonNull Gson gson, @NonNull Type rawType, Type[] actualTypes) {
        var listType = TypeToken.getParameterized(rawType, actualTypes).getType();
        return gson.fromJson(json, listType);
    }
}
