package com.au.aulitesql.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.au.aulitesql.Entity;
import java.util.HashMap;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 */
public class EntityMapDao<K, E extends Entity> extends DefEntityMapDao<K, E> {
    private final MutableLiveData<HashMap<K, E>> liveData;

    /**
     * 获取用于监听的LiveData。
     */
    public LiveData<HashMap<K, E>> getReadLiveData() {
        return liveData;
    }


    public EntityMapDao(Class<E> entityClass, @NonNull String keyField) {
        super(entityClass, keyField);
        liveData = new MutableLiveData<>();
    }

    @Override
    public void onNewMapGenerated(HashMap<K, E> map) {
        liveData.postValue(map);
    }
}