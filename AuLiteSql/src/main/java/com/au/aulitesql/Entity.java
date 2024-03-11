package com.au.aulitesql;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.au.aulitesql.annotation.AuName;
import com.au.aulitesql.info.UnImplmentionException;
import com.au.aulitesql.util.GsonUtil;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * @author au
 * @date :2023/11/10 11:48
 * @description: 继承本类的，就会被创建一张表。
 *
 * 数据库表类，必须有一个空构造函数。
 */
public class Entity implements BaseColumns {
    public static final String _ID_WHERE_CAUSE = BaseColumns._ID + " = ?";

    @AuName(BaseColumns._ID)
    private long id = -1;

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
    public final void pack(@NonNull ContentValues cv) throws IllegalAccessException, UnImplmentionException {
        var tableInfo = AuLiteSql.getInstance().getCreateInfo().getByTableClass(getClass());
        if (tableInfo != null) {
            for (var fi : tableInfo.fieldInfoList) {
                switch (fi.field.getType().getSimpleName()) {
                    case "long", "Long" -> {
                        cv.put(fi.name, fi.field.getLong(this));
                    }
                    case "int", "Integer" -> {
                        cv.put(fi.name, fi.field.getInt(this));
                    }
                    case "short", "Short" -> {
                        cv.put(fi.name, fi.field.getShort(this));
                    }
                    case "byte", "Byte" -> {
                        cv.put(fi.name, fi.field.getByte(this));
                    }
                    case "char", "Character" -> {
                        cv.put(fi.name, (int) fi.field.getChar(this));
                    }
                    case "float", "Float" -> {
                        cv.put(fi.name, fi.field.getFloat(this));
                    }
                    case "double", "Double" -> {
                        cv.put(fi.name, fi.field.getDouble(this));
                    }
                    case "boolean", "Boolean" -> {
                        cv.put(fi.name, fi.field.getBoolean(this));
                    }
                    case "String" -> {
                        var v = fi.field.get(this);
                        cv.put(fi.name, v != null ? v.toString() : null);
                    }

                    case "List", "ArrayList", "Map", "HashMap", "Set", "HashSet" -> {
                        cv.put(fi.name, gson().toJson(fi.field.get(this)));
                    }

                    case "byte[]" -> {
                        cv.put(fi.name, (byte[])fi.field.get(this));
                    }
                    default -> {
                        throw new UnImplmentionException("Entity field not support >"+ fi.name + "< type:" + fi.field.getType().getSimpleName());
                    }
                }
            }
        }
        cv.remove("id");
        cv.remove("_id");
    }

    /**
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
    public final void unpack(@NonNull Cursor itemCursor) throws IllegalAccessException, UnImplmentionException {
        var columnID_id = itemCursor.getColumnIndex(BaseColumns._ID);
        if (columnID_id >= 0) setId(itemCursor.getLong(columnID_id));

        var tableInfo = AuLiteSql.getInstance().getCreateInfo().getByTableClass(getClass());
        if (tableInfo != null) {
            var errorCollectionList = new ArrayList<String>();
            for (var fi : tableInfo.fieldInfoList) {
                int columnIndex = itemCursor.getColumnIndex(fi.name);
                if (columnIndex >= 0) {
                    switch (fi.field.getType().getSimpleName()) {
                        case "long", "Long" -> {
                            fi.field.setLong(this, itemCursor.getLong(columnIndex));
                        }
                        case "int", "Integer" -> {
                            fi.field.setInt(this, itemCursor.getInt(columnIndex));
                        }
                        case "short", "Short" -> {
                            fi.field.setShort(this, itemCursor.getShort(columnIndex));
                        }
                        case "byte", "Byte" -> {
                            fi.field.setByte(this, (byte) itemCursor.getInt(columnIndex));
                        }
                        case "char", "Character" -> {
                            fi.field.setChar(this, (char) itemCursor.getInt(columnIndex));
                        }
                        case "float", "Float" -> {
                            fi.field.setFloat(this, itemCursor.getFloat(columnIndex));
                        }
                        case "double", "Double" -> {
                            fi.field.setDouble(this, itemCursor.getDouble(columnIndex));
                        }
                        case "boolean", "Boolean" -> {
                            fi.field.setBoolean(this, itemCursor.getInt(columnIndex) != 0);
                        }
                        case "String" -> {
                            fi.field.set(this, itemCursor.getString(columnIndex));
                        }

                        case "List", "ArrayList", "Map", "HashMap", "Set", "HashSet" -> {
                            var value = GsonUtil.gsonFromGeneric(itemCursor.getString(columnIndex), gson(), fi.field);
                            fi.field.set(this, value);
                        }

                        case "byte[]" -> {
                            fi.field.set(this, itemCursor.getBlob(columnIndex));
                        }
                        default -> {
                            errorCollectionList.add("Entity field not support >"+ fi.name + "< type:" + fi.field.getType().getSimpleName());
                        }
                    }
                }
            }

            if (!errorCollectionList.isEmpty()) {
                var sb = new StringBuilder();
                for (var s : errorCollectionList) {
                    sb.append(s).append("\n");
                }
                throw new UnImplmentionException(getClass() + "\n" + sb.toString());
            }
        }
    }

    /**
     * 类似pack等函数，重置所有数据。
     */
    public final void reset() throws IllegalAccessException {
        var tableInfo = AuLiteSql.getInstance().getCreateInfo().getByTableClass(getClass());
        if (tableInfo != null) {
            for (var fi : tableInfo.fieldInfoList) {
                fi.field.set(this, fi.defaultValue);
            }
        }
    }

    @NonNull
    public Gson gson() {
        return AuLiteSql.getGsonOrNew();
    }
}
