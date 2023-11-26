package com.au.aulitesql;

import androidx.annotation.NonNull;

import com.au.aulitesql.actions.ICallback;

import java.util.List;

public abstract class BaseEntityTableDao<E extends Entity> {
    public final Class<E> entityClass;
    public BaseEntityTableDao(Class<E> clazz) {
        this.entityClass = clazz;
    }

    ////////////load////////////////
    public void loadAllFilter(String fieldName, Object value) {
        loadAllFilter(fieldName, value, null);
    }
    public abstract void loadAllFilter(String fieldName, Object value, ICallback<List<E>> callback);

    public void loadAll() {
        loadAll(null);
    }
    public abstract void loadAll(ICallback<List<E>> callback);

    public void loadAll(@NonNull String fieldName, Object value,
                                 String groupBy, String having, String orderBy) {
        loadAll(fieldName, value, groupBy, having, orderBy, null);
    }

    public abstract void loadAll(@NonNull String fieldName, Object value,
                                       String groupBy, String having, String orderBy, ICallback<List<E>> callback);

    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                 String groupBy, String having, String orderBy) {
        loadAll(selections, selectionArgs, groupBy, having, orderBy, null);
    }

    public abstract void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                       String groupBy, String having, String orderBy, ICallback<List<E>> callback);

    public void rawLoadAll(String sql, String[] selectionArgs) {
        rawLoadAll(sql, selectionArgs, null);
    }

    public abstract void rawLoadAll(String sql, String[] selectionArgs, ICallback<List<E>> callback);

    //////////////////delete////////////////

    public void deleteAll(List<E> dataList) {
        deleteAll(dataList, null);
    }
    public abstract void deleteAll(List<E> dataList, ICallback<Integer> deleteCountCallback);

    public void delete(E instance) {
        delete(instance, null);
    }

    public abstract void delete(E instance, ICallback<Boolean> deleteSuccessCallback);

    public abstract void clear(ICallback<Boolean> clearSuccessCallback);

    ///////////////////save/////////////////
    public void save(E instance) {
        save(instance, null);
    }
    public abstract void save(E instance, ICallback<Boolean> saveSuccessCallback);

    public void saveAll(List<E> dataList) {
        saveAll(dataList, null);
    }

    public abstract void saveAll(List<E> dataList, ICallback<Integer> saveSuccessCountCallback);

    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
}