package com.au.aulitesql.dao;

import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.BaseEntityListDao;
import com.au.aulitesql.Entity;
import com.au.aulitesql.actions.ICallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 * @param <E>
 */
public abstract class DefEntityListDao<E extends Entity> extends BaseEntityListDao<E> {
    private final List<E> origData = new ArrayList<>();

    public DefEntityListDao(Class<E> entityClass) {
        super(entityClass);
    }

    protected abstract void onNewListGenerated(List<E> list);

    @Override
    public void loadAllFilter(String fieldName, Object value, ICallback<List<E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAllFilter(entityClass, fieldName, value);
            origData.clear();
            origData.addAll(list);
            onNewListGenerated(list);
            if (callback != null) callback.callback(list);
        });
    }

    @Override
    public void loadAll(ICallback<List<E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass);
            origData.clear();
            origData.addAll(list);
            onNewListGenerated(list);
            if (callback != null) callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String fieldName, Object value, String groupBy, String having, String orderBy, ICallback<List<E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, fieldName, value, groupBy, having, orderBy);
            origData.clear();
            origData.addAll(list);
            onNewListGenerated(list);
            if (callback != null) callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs, String groupBy, String having, String orderBy, ICallback<List<E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, selections, selectionArgs, groupBy, having, orderBy);
            origData.clear();
            origData.addAll(list);
            onNewListGenerated(list);
            if (callback != null) callback.callback(list);
        });
    }

    @Override
    public void rawLoadAll(String sql, String[] selectionArgs, ICallback<List<E>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().rawLoadAll(entityClass, sql, selectionArgs);
            origData.clear();
            origData.addAll(list);
            onNewListGenerated(list);
            if (callback != null) callback.callback(list);
        });
    }

    @Override
    public void deleteAll(List<E> dataList, ICallback<Integer> deleteCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var deletedList = AuLiteSql.getDao().deleteAllBackDeleted(dataList);
            deletedList.forEach(origData::remove);
            onNewListGenerated(origData);
            if(deleteCountCallback != null) deleteCountCallback.callback(deletedList.size());
        });
    }

    @Override
    public void delete(E instance, ICallback<Boolean> deleteSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().delete(instance);
            if (suc) {
                origData.remove(instance);
                onNewListGenerated(origData);
            }

            if(deleteSuccessCallback != null) deleteSuccessCallback.callback(suc);
        });
    }

    @Override
    public void clear(ICallback<Boolean> clearSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().clear(entityClass);
            if (suc) {
                origData.clear();
                onNewListGenerated(origData);
            }

            if(clearSuccessCallback != null) clearSuccessCallback.callback(suc);
        });
    }

    @Override
    public void save(E instance, ICallback<Boolean> saveSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var ins = AuLiteSql.getDao().save(instance);
            if (ins != null) {
                origData.add(instance);
                onNewListGenerated(origData);
            }
            if (saveSuccessCallback != null) saveSuccessCallback.callback(ins != null);
        });
    }

    @Override
    public void saveAll(List<E> dataList, ICallback<Integer> saveSuccessCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var savedList = AuLiteSql.getDao().saveAllBackSavedList(dataList);
            origData.addAll(savedList);
            onNewListGenerated(origData);
            if(saveSuccessCountCallback != null) saveSuccessCountCallback.callback(savedList.size());
        });
    }

}