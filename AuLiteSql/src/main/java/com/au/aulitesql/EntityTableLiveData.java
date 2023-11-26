package com.au.aulitesql;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.au.aulitesql.actions.ICallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 * @param <Ent>
 */
public class EntityTableLiveData<Ent extends Entity> extends BaseEntityTableDao<Ent> {
    private final List<Ent> origData = new ArrayList<>();

    private final MutableLiveData<List<Ent>> liveData;
    public EntityTableLiveData(MutableLiveData<List<Ent>> liveData, Class<Ent> entityClass) {
        super(entityClass);
        this.liveData = liveData;
    }

    @Override
    public void loadAllFilter(String fieldName, Object value, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAllFilter(entityClass, fieldName, value);
            origData.clear();
            origData.addAll(list);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass);
            origData.clear();
            origData.addAll(list);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String fieldName, Object value, String groupBy, String having, String orderBy, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, fieldName, value, groupBy, having, orderBy);
            origData.clear();
            origData.addAll(list);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void loadAll(@NonNull String selections, @NonNull String[] selectionArgs, String groupBy, String having, String orderBy, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().loadAll(entityClass, selections, selectionArgs, groupBy, having, orderBy);
            origData.clear();
            origData.addAll(list);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void rawLoadAll(String sql, String[] selectionArgs, ICallback<List<Ent>> callback) {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getDao().rawLoadAll(entityClass, sql, selectionArgs);
            origData.clear();
            origData.addAll(list);
            liveData.postValue(list);
            callback.callback(list);
        });
    }

    @Override
    public void deleteAll(List<Ent> dataList, ICallback<Integer> deleteCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var deletedList = AuLiteSql.getDao().deleteAllBackDeleted(dataList);
            deletedList.forEach(origData::remove);
            liveData.postValue(new ArrayList<>(origData));
            deleteCountCallback.callback(deletedList.size());
        });
    }

    @Override
    public void delete(Ent instance, ICallback<Boolean> deleteSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().delete(instance);
            if (suc) {
                origData.remove(instance);
            }

            liveData.postValue(new ArrayList<>(origData));
            deleteSuccessCallback.callback(suc);
        });
    }

    @Override
    public void clear(ICallback<Boolean> clearSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var suc = AuLiteSql.getDao().clear(entityClass);
            if (suc) {
                origData.clear();
                liveData.postValue(new ArrayList<>(origData));
            }

            clearSuccessCallback.callback(suc);
        });
    }

    @Override
    public void save(Ent instance, ICallback<Boolean> saveSuccessCallback) {
        AuLiteSql.getInstance().execute(()->{
            var ins = AuLiteSql.getDao().save(instance);
            if (ins != null) {
                origData.add(instance);
            }

            liveData.postValue(new ArrayList<>(origData));
            saveSuccessCallback.callback(ins != null);
        });
    }

    @Override
    public void saveAll(List<Ent> dataList, ICallback<Integer> saveSuccessCountCallback) {
        AuLiteSql.getInstance().execute(()->{
            var savedList = AuLiteSql.getDao().saveAllBackSavedList(dataList);
            origData.addAll(savedList);
            liveData.postValue(new ArrayList<>(origData));
            saveSuccessCountCallback.callback(savedList.size());
        });
    }

}