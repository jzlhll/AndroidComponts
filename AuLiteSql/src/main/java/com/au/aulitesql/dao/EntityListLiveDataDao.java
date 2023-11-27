package com.au.aulitesql.dao;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.au.aulitesql.Entity;
import java.util.List;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 * @param <E>
 */
public class EntityListLiveDataDao<E extends Entity> extends DefEntityListDao<E> {

    private final MutableLiveData<List<E>> liveData;
    /**
     * 获取用于监听的LiveData。
     */
    public LiveData<List<E>> getReadLiveData() {
        return liveData;
    }

    public EntityListLiveDataDao(Class<E> entityClass) {
        super(entityClass);
        this.liveData = new MutableLiveData<>();
    }

    @Override
    protected void onNewListGenerated(List<E> list) {
        liveData.postValue(list);
    }
}