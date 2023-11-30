package com.au.aulitesql;

import androidx.annotation.NonNull;

import java.util.List;

public interface IDao {
    <E extends Entity> List<E> loadAllFilter(Class<E> clazz, String fieldName, Object value);
    <E extends Entity> List<E> loadAll(Class<E> clazz);
    <E extends Entity> List<E> loadAll(Class<E> clazz, @NonNull String fieldName, Object value,
                                       String groupBy, String having, String orderBy);

    <E extends Entity> List<E> loadAll(Class<E> clazz, @NonNull String selections, @NonNull String[] selectionArgs,
                                       String groupBy, String having, String orderBy);

    <E extends Entity> List<E> rawLoadAll(Class<E> clazz, String sql, String[] selectionArgs);

    //////////////////////////////////
    //////////////////////////////////

    <E extends Entity> int deleteAll(List<E> dataList);
    <E extends Entity> List<E> deleteAllBackDeleted(List<E> dataList);

    <E extends Entity> boolean clear(Class<E> clazz);

    boolean delete(Entity instance);

    //////////////////////////////////
    //////////////////////////////////

    Entity save(Entity instance);

    /**
     * @param status 请传入 new boolean[] {false}。用来接收插入是否成功结果。
     */
    Entity save(Entity instance, @NonNull boolean[] status);
    <E extends Entity> int saveAll(List<E> dataList);
    <E extends Entity> List<E> saveAllBackSavedList(List<E> dataList);

    //////////////////////////////////
    //////////////////////////////////
}