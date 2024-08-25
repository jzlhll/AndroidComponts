package com.au.aulitesql.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.BaseEntityMapDao;
import com.au.aulitesql.Entity;
import com.au.aulitesql.actions.ICallback;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 */
public abstract class DefEntityMapDao<K, E extends Entity> extends BaseEntityMapDao<K, E> {
    private final Map<K, E> origData = new HashMap<>();

    private Field keyFieldField;
    private final String keyField;

    public DefEntityMapDao(Class<E> entityClass, @NonNull String keyField) {
        super(entityClass, keyField);
        this.keyField = keyField;
    }

    public abstract void onNewMapGenerated(HashMap<K, E> map);

    private Field getKeyField() {
        if (keyFieldField != null) {
            return keyFieldField;
        }
        for (Class<?> superClass = entityClass;
             superClass != null && superClass != Object.class;
             superClass = superClass.getSuperclass()) {
            try {
                keyFieldField = superClass.getDeclaredField(keyField);
                return keyFieldField;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Please give the correct key field! " + keyField + " is error.");
    }

    private K getKeyFieldValue(E instance) {
        try {
            return (K) getKeyField().get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Please check your generic type <K> for EntityMapLiveData");
        }
    }

    @Override
    public void loadAllFilter(String fieldName, Object value, ICallback<Map<K, E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAllFilter(entityClass, fieldName, value);
            origData.clear();
            var map = new HashMap<K, E>();
            for (var item : list) {
                map.put(getKeyFieldValue(item), item);
            }
            origData.putAll(map);
            onNewMapGenerated(map);
            if (callback != null) callback.callback(map);
        });
    }

    @Override
    public void loadAll(ICallback<Map<K, E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass);
            origData.clear();
            var map = new HashMap<K, E>();
            for (var item : list) {
                map.put(getKeyFieldValue(item), item);
            }
            origData.putAll(map);
            onNewMapGenerated(map);
            if (callback != null) callback.callback(map);
        });
    }

    @Override
    public void loadAll(@NonNull String fieldName, Object value, String groupBy, String having, String orderBy, ICallback<Map<K, E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, fieldName, value, groupBy, having, orderBy);
            origData.clear();
            var map = new HashMap<K, E>();
            for (var item : list) {
                map.put(getKeyFieldValue(item), item);
            }
            origData.putAll(map);
            onNewMapGenerated(map);
            if (callback != null) callback.callback(map);
        });
    }

    @Override
    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs, String groupBy, String having, String orderBy, ICallback<Map<K, E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, selections, selectionArgs, groupBy, having, orderBy);
            origData.clear();
            var map = new HashMap<K, E>();
            for (var item : list) {
                map.put(getKeyFieldValue(item), item);
            }
            origData.putAll(map);
            onNewMapGenerated(map);
            if (callback != null) callback.callback(map);
        });
    }

    @Override
    public void rawLoadAll(String sql, String[] selectionArgs, ICallback<Map<K, E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().rawLoadAll(entityClass, sql, selectionArgs);
            origData.clear();
            var map = new HashMap<K, E>();
            for (var item : list) {
                map.put(getKeyFieldValue(item), item);
            }
            origData.putAll(map);
            onNewMapGenerated(map);
            if (callback != null) callback.callback(map);
        });
    }

    @Override
    public void deleteKeyAll(List<K> keyList, ICallback<Integer> deleteCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var shouldDeletedList = new ArrayList<E>();
            for (var key : keyList) {
                var removed = origData.remove(key);
                if (removed != null) {
                    shouldDeletedList.add(removed);
                }
            }
            var count = AuLiteSql.getDao().deleteAll(shouldDeletedList);
            onNewMapGenerated(new HashMap<>(origData));
            //todo 可能对不上size
            if (deleteCountCallback != null) deleteCountCallback.callback(count);
        });
    }

    @Override
    public void deleteKey(@NonNull K key, ICallback<Boolean> deleteSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var value = origData.remove(key);
            var suc = false;
            if (value != null) {
                suc = AuLiteSql.getDao().delete(value);
            }
            if (suc) {
                onNewMapGenerated(new HashMap<>(origData));
            }
            if (deleteSuccessCallback != null) deleteSuccessCallback.callback(suc);
        });
    }

    @Override
    public void deleteValue(@NonNull E value, ICallback<Boolean> deleteSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = false;
            suc = AuLiteSql.getDao().delete(value);
            if (suc) {
                onNewMapGenerated(new HashMap<>(origData));
            }
            if (deleteSuccessCallback != null) deleteSuccessCallback.callback(suc);
        });
    }

    @Override
    public void clear(ICallback<Boolean> clearSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().clear(entityClass);
            if (suc) {
                origData.clear();
                onNewMapGenerated(new HashMap<>());
            }

            if (clearSuccessCallback != null) clearSuccessCallback.callback(suc);
        });
    }

    @Override
    public void save(@NonNull K key, @Nullable E value, ICallback<Boolean> saveSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var ins = AuLiteSql.getDao().save(value);
            if (ins != null) {
                origData.put(key, value);
                onNewMapGenerated(new HashMap<>(origData));
            }

            if (saveSuccessCallback != null) saveSuccessCallback.callback(ins != null);
        });
    }

    @Override
    public void saveAll(HashMap<K, E> map, ICallback<Integer> saveSuccessCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var savedList = AuLiteSql.getDao().saveAllBackSavedList(new ArrayList<>(map.values()));
            //todo 可能size对不上。
            origData.putAll(map);
            onNewMapGenerated(new HashMap<>(origData));
            if (saveSuccessCountCallback != null) saveSuccessCountCallback.callback(savedList.size());
        });
    }
}