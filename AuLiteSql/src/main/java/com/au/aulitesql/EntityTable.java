package com.au.aulitesql;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.au.aulitesql.annotation.AuName;
import com.google.gson.Gson;

/**
 * @author allan.jiang
 * @date :2023/11/10 11:48
 * @description: 继承本类的，就会被创建一张表。
 *
 * 数据库表类，必须有一个空构造函数。
 */
public abstract class EntityTable implements BaseColumns {
    public static final String _ID_WHERE_CAUSE = BaseColumns._ID + " = ?";

    @AuName(BaseColumns._ID)
    private long id;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    /**
     *
     * @return 本数据是否已经同步到数据库中。
     */
    public final boolean isSyncedToDb() {
        return id >= 0;
    }

    /**
     * 日后，会被自动化。
     * 打包：打包的意思，就是将数据写入到本地数据库。<br>
     * 任何子类增加了字段，都需要实现它。有子类一层一层继承，则一层一层super，往里面写入数据。
     * 比如子类有2个字段:
     * <br>
     * String name<br>
     * AuAltName("altAge")<br>
     * int age<br><br>
     * 则需要写入：<br>
     cv.putString("name", name);<br>
     cv.putString("altAge", age);
     */
    public abstract void pack(@NonNull ContentValues cv);

    /**
     * 日后，会被自动化。
     * 解包：解包的意思，就是将数据从本地数据库导出来。<br>
     * 与pack相反，这里是从数据库中提取出内容。类似的，也需要一类一层一层实现，去解析。<br>
     * 比如某个子类有2个字段：
     * <br>
     String name<br>
     AuAltName("altAge")<br>
     int age<br><br>
     * 则需要写为：<br>
        int columnId = cursor.getColumnIndex("name");  <br>
        if (columnId >= 0) name = cursor.getString(columnId); <br>
     <br>
        columnId = cursor.getColumnIndex("altAge");  <br>
        if (columnId >= 0) age = cursor.getInt(columnId); <br>
     */
    public abstract void unpack(@NonNull Cursor cursor);

    /**
     * 日后，会被自动化。
     * 重置：重置的意思，就是将所有数据恢复成默认。
     * 类似pack等函数，重置所有数据。
     */
    public abstract void reset();

    public final boolean delete() {
        var r = AuLiteSql.getInstance().dao.delete(this);
        reset();
        id = -1;
        return r;
    }

    /**
     * 增加或者修改。
     */
    public final long save() {
        AuLiteSql.getInstance().dao.save(this);
        return id;
    }

    @NonNull
    public Gson gson() {
        return AuLiteSql.getGsonOrNew();
    }
}
