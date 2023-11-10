package com.au.module_ausqlite;

import android.provider.BaseColumns;

/**
 * @author allan.jiang
 * @date :2023/11/10 11:48
 * @description: 继承本类的，就会被创建一张表。
 */
public abstract class EntityTable implements BaseColumns {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
