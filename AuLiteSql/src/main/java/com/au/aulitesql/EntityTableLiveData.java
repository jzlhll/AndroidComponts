package com.au.aulitesql;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.au.aulitesql.actions.ICallback;

import java.util.List;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 * @param <Ent>
 */
public class EntityTableLiveData<Ent extends Entity> extends BaseEntityTableDao<Ent> {
    private final MutableLiveData<List<Ent>> liveData;
    public EntityTableLiveData(MutableLiveData<List<Ent>> liveData, Class<Ent> entityClass) {
        super(entityClass);
        this.liveData = liveData;
    }

    @Override
    public void loadAllFilter(String fieldName, Object value, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAllFilter(entityClass, fieldName, value);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String fieldName, Object value, String groupBy, String having, String orderBy, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, fieldName, value, groupBy, having, orderBy);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs, String groupBy, String having, String orderBy, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, selections, selectionArgs, groupBy, having, orderBy);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void rawLoadAll(String sql, String[] selectionArgs, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().rawLoadAll(entityClass, sql, selectionArgs);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void deleteAll(List<Ent> dataList, ICallback<Integer> deleteCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var deletedList = AuLiteSql.getDao().deleteAllBackDeleted(dataList);
            var curList = liveData.getValue();
            if (curList != null) {
                deletedList.forEach(curList::remove);
            }

            liveData.postValue(curList);
            deleteCountCallback.callback(deletedList.size());
        });
    }

    @Override
    public void delete(Ent instance, ICallback<Boolean> deleteSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().delete(instance);
            var curList = liveData.getValue();
            if (suc && curList != null) {
                curList.remove(instance);
            }

            liveData.postValue(curList);
            deleteSuccessCallback.callback(suc);
        });
    }

    @Override
    public void save(Ent instance, ICallback<Boolean> saveSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var ins = AuLiteSql.getDao().save(instance);
            var curList = liveData.getValue();
            if (ins != null && curList != null) {
                curList.add(instance);
            }

            liveData.postValue(curList);
            saveSuccessCallback.callback(ins != null);
        });
    }

    @Override
    public void saveAll(List<Ent> dataList, ICallback<Integer> saveSuccessCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var savedList = AuLiteSql.getDao().saveAllBackSavedList(dataList);
            var curList = liveData.getValue();
            if (curList != null) {
                curList.addAll(savedList);
            } else {
                curList = savedList;
            }

            liveData.postValue(curList);
            saveSuccessCountCallback.callback(savedList.size());
        });
    }

}