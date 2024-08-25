package com.au.aulitesql;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.actions.ICallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseEntityMapDao<K, E extends Entity> {
    public final Class<E> entityClass;
    public final String keyField;

    public BaseEntityMapDao(Class<E> clazz, String keyField) {
        this.entityClass = clazz;
        this.keyField = keyField;
    }

    ////////////load////////////////
    public void loadAllFilter(String fieldName, Object value) {
        loadAllFilter(fieldName, value, null);
    }
    public abstract void loadAllFilter(String fieldName, Object value, ICallback<Map<K, E>> callback);

    public void loadAll() {
        loadAll(null);
    }
    public abstract void loadAll(ICallback<Map<K, E>> callback);

    public void loadAll(@NonNull String fieldName, Object value,
                                 String groupBy, String having, String orderBy) {
        loadAll(fieldName, value, groupBy, having, orderBy, null);
    }

    public abstract void loadAll(@NonNull String fieldName, Object value,
                                       String groupBy, String having, String orderBy, ICallback<Map<K, E>> callback);

    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                 String groupBy, String having, String orderBy) {
        loadAll(selections, selectionArgs, groupBy, having, orderBy, null);
    }

    public abstract void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                       String groupBy, String having, String orderBy, ICallback<Map<K, E>> callback);

    public void rawLoadAll(String sql, String[] selectionArgs) {
        rawLoadAll(sql, selectionArgs, null);
    }

    public abstract void rawLoadAll(String sql, String[] selectionArgs, ICallback<Map<K, E>> callback);

    //////////////////delete////////////////

    public void deleteKeyAll(List<K> keyList) {
        deleteKeyAll(keyList, null);
    }
    public abstract void deleteKeyAll(List<K> keyList, ICallback<Integer> deleteCountCallback);

    public void deleteKey(@NonNull K key) {
        deleteKey(key, null);
    }

    public abstract void deleteKey(@NonNull K key, ICallback<Boolean> deleteSuccessCallback);

    public void deleteValue(@NonNull E value) {
        deleteValue(value, null);
    }

    public abstract void deleteValue(@NonNull E value, ICallback<Boolean> deleteSuccessCallback);

    public abstract void clear(ICallback<Boolean> clearSuccessCallback);

    ///////////////////save/////////////////
    public void save(@NonNull K key, @NonNull E value) {
        save(key, value, null);
    }
    public abstract void save(@NonNull K key, @Nullable E value, ICallback<Boolean> saveSuccessCallback);

    public void saveAll(HashMap<K, E> map) {
        saveAll(map, null);
    }

    public abstract void saveAll(HashMap<K, E> map, ICallback<Integer> saveSuccessCountCallback);

    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
}