package com.au.aulitesql;

import androidx.annotation.NonNull;

import java.util.List;

public interface IDao {
    <E extends EntityTable> List<E> loadAllFilter(Class<E> clazz, String fieldName, Object value);
    <E extends EntityTable> List<E> loadAll(Class<E> clazz);
    <E extends EntityTable> List<E> loadAll(Class<E> clazz, @NonNull String fieldName, Object value,
                                                String groupBy, String having, String orderBy);

    <E extends EntityTable> List<E> loadAll(Class<E> clazz, @NonNull String selections, @NonNull String[] selectionArgs,
                                                   String groupBy, String having, String orderBy);

    <E extends EntityTable> List<E> rawLoadAll(Class<E> clazz, String sql, String[] selectionArgs);

    //////////////////////////////////
    //////////////////////////////////

    <E extends EntityTable> int deleteAll(List<E> dataList);

    boolean delete(EntityTable instance);

    //////////////////////////////////
    //////////////////////////////////

    EntityTable save(EntityTable instance);

    /**
     * @param status 请传入 new boolean[] {false}。用来接收插入是否成功结果。
     */
    EntityTable save(EntityTable instance, @NonNull boolean[] status);
    <E extends EntityTable> int saveAll(List<E> dataList);

    //////////////////////////////////
    //////////////////////////////////

}