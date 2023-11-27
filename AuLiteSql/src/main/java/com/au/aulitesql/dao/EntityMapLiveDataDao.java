package com.au.aulitesql.dao;

import androidx.annotation.NonNull;

import com.au.aulitesql.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 */
public class EntityMapLiveDataDao<K, E extends Entity> extends DefEntityMapDao<K, E> {
    private final Map<K, E> origData = new HashMap<>();

    private HashMap<K, E> data;
    public Map<K, E> getData() {
        return data;
    }

    public EntityMapLiveDataDao(Class<E> entityClass, @NonNull String keyField) {
        super(entityClass, keyField);
    }

    @Override
    public void onNewMapGenerated(HashMap<K, E> map) {
        data = map;
    }
}