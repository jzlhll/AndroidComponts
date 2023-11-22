package com.au.aulitesql;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class EntityTableLiveData<E extends Entity> {

    private final MutableLiveData<List<E>> liveData;
    private final Class<E> entityClass;

    public EntityTableLiveData(MutableLiveData<List<E>> liveData, Class<E> entityClass) {
        this.liveData = liveData;
        this.entityClass = entityClass;
    }

    public EntityTableLiveData(Class<E> entityClass) {
        this.liveData = new MutableLiveData<>();
        this.entityClass = entityClass;
    }

    /**
     * 加载整张表
     */
    public EntityTableLiveData<?> loadTable() {
        AuLiteSql.getInstance().execute(()->{
            var list = AuLiteSql.getInstance().dao.loadAll(entityClass);
            liveData.postValue(list);
        });
        return this;
    }
}