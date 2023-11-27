package com.au.aulitesql.dao;

import androidx.annotation.Nullable;
import com.au.aulitesql.Entity;
import java.util.List;

/**
 * 加载整张表的LiveData。并且当更新其中的数据或者插入有变化的时候，会得到通知。
 * @param <E>
 */
public class EntityListDao<E extends Entity> extends DefEntityListDao<E> {

    private List<E> data;

    @Nullable
    public List<E> getData() {return data;}

    public EntityListDao(Class<E> entityClass) {
        super(entityClass);
    }

    @Override
    protected void onNewListGenerated(List<E> list) {
        data = list;
    }
}