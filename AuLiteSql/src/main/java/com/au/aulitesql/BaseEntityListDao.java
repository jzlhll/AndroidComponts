package com.au.aulitesql;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.au.aulitesql.actions.ICallback;

import java.util.List;

public abstract class BaseEntityListDao<E extends Entity> {
    public final Class<E> entityClass;
    public BaseEntityListDao(Class<E> clazz) {
        this.entityClass = clazz;
    }

    ////////////load////////////////
    @WorkerThread
    public void loadAllFilter(String fieldName, Object value) {
        loadAllFilter(fieldName, value, null);
    }
    @WorkerThread
    public abstract void loadAllFilter(String fieldName, Object value, ICallback<List<E>> callback);

    @WorkerThread
    public void loadAll() {
        loadAll(null);
    }
    @WorkerThread
    public abstract void loadAll(ICallback<List<E>> callback);

    @WorkerThread
    public void loadAll(@NonNull String fieldName, Object value,
                                 String groupBy, String having, String orderBy) {
        loadAll(fieldName, value, groupBy, having, orderBy, null);
    }

    @WorkerThread
    public abstract void loadAll(@NonNull String fieldName, Object value,
                                       String groupBy, String having, String orderBy, ICallback<List<E>> callback);

    @WorkerThread
    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                 String groupBy, String having, String orderBy) {
        loadAll(selections, selectionArgs, groupBy, having, orderBy, null);
    }

    @WorkerThread
    public abstract void loadAll(@NonNull String selections, @NonNull String[] selectionArgs,
                                       String groupBy, String having, String orderBy, ICallback<List<E>> callback);

    @WorkerThread
    public void rawLoadAll(String sql, String[] selectionArgs) {
        rawLoadAll(sql, selectionArgs, null);
    }

    @WorkerThread
    public abstract void rawLoadAll(String sql, String[] selectionArgs, ICallback<List<E>> callback);

    //////////////////delete////////////////

    @WorkerThread
    public void deleteAll(List<E> dataList) {
        deleteAll(dataList, null);
    }

    @WorkerThread
    public abstract void deleteAll(List<E> dataList, ICallback<Integer> deleteCountCallback);

    @WorkerThread
    public void delete(E instance) {
        delete(instance, null);
    }

    @WorkerThread
    public abstract void delete(E instance, ICallback<Boolean> deleteSuccessCallback);

    @WorkerThread
    public abstract void clear(ICallback<Boolean> clearSuccessCallback);

    ///////////////////save/////////////////
    @WorkerThread
    public void save(E instance) {
        save(instance, null);
    }

    @WorkerThread
    public abstract void save(E instance, ICallback<Boolean> saveSuccessCallback);

    @WorkerThread
    public void saveAll(List<E> dataList) {
        saveAll(dataList, null);
    }

    @WorkerThread
    public abstract void saveAll(List<E> dataList, ICallback<Integer> saveSuccessCountCallback);

    //////////////////////////////////
    @WorkerThread
    public void update(E instance) {
        update(instance, null);
    }

    @WorkerThread
    public void update(E instance, @Nullable ICallback<Boolean> updateSuccessCallback) {
        var id = saveInstanceToDb(instance);
        if(updateSuccessCallback != null) updateSuccessCallback.callback(id >= 0L);
    }

    /**
     * 增加或者修改。
     */
    @WorkerThread
    public static long saveInstanceToDb(Entity entity) {
        AuLiteSql.getDao().save(entity);
        return entity.getId();
    }

    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
}