package com.au.aulitesql;

import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

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

    public long saveTo() {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
        }
    }

    public abstract <T extends EntityTable> T fromOneLineCursor(@NonNull Cursor cursor);
}
