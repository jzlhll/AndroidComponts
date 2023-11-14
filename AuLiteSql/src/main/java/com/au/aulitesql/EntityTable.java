package com.au.aulitesql;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.au.aulitesql.annotation.AuName;

/**
 * @author allan.jiang
 * @date :2023/11/10 11:48
 * @description: 继承本类的，就会被创建一张表。
 */
public abstract class EntityTable implements BaseColumns {
    public static final String _ID_WHERE_CAUSE = BaseColumns._ID + " = ?";

    @AuName(BaseColumns._ID)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isRealData() {
        return id >= 0;
    }

    /**
     * 都需要实现它。往里面赛数据
     */
    public abstract void prepareDbData(@NonNull ContentValues cv);

    public abstract void setFieldFromDbCursor(@NonNull Cursor cursor, int columnIndex, @NonNull String columnName);

    public abstract void resetSelf();

    public boolean delete() {
        var r = AuLiteSql.deleteData(this);
        resetSelf();
        id = -1;
        return r;
    }

    /**
     * 增加，修改。
     */
    public long save() {
        id = AuLiteSql.saveData(this);
        return id;
    }
}
