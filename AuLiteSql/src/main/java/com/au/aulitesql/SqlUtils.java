package com.au.aulitesql;

import static com.au.aulitesql.TableCreators.primaryKey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

//常用sql语句大全 或者 代码片段
public final class SqlUtils {
    public static final class SqlString {
        public static final String queryTableNameFromDb
                = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'";

        public static final String queryAllTableNames
                = "SELECT name FROM sqlite_master WHERE type='table'";

        private static final String sqlDeleteOneTab
                = "DROP TABLE IF EXISTS %s";

        private static final String sqlRenameTable
                = "ALTER TABLE %s RENAME TO %s";

        public static final String sqlCreateLogTable
                = "CREATE TABLE IF NOT EXISTS au_lite_log ("
                    + "tableName TEXT PRIMARY KEY, \n" +
                "createStr TEXT, \n" +
                "extra TEXT);";

        /**
         * 删除一张表的String
         */
        public static String sqlDeleteTable(@NonNull String tableName) {
            return String.format(sqlDeleteOneTab, tableName);
        }

        public static String sqlRenameTable(@NonNull String tableName, @Nullable String tempPrefix) {
            if (tempPrefix == null || tempPrefix.isEmpty()) {
                tempPrefix = "_temp_";
            }
            return String.format(sqlRenameTable, tableName, tempPrefix + tableName);
        }
    }


    /**
     * 是否包含某个table
     */
    @WorkerThread
    public static boolean hasTable(@NonNull SQLiteDatabase db, @NonNull String tableName) {
        String query = String.format(SqlString.queryTableNameFromDb, tableName);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }
        return false;
    }

    /**
     * 查询所有的table名字
     */
    @WorkerThread
    @NonNull
    public static List<String> allTableNames(@NonNull SQLiteDatabase db) {
        List<String> tableNames = new ArrayList<>();
        Cursor cursor = db.rawQuery(SqlString.queryAllTableNames, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int columnId = cursor.getColumnIndex("name");
                if (columnId >= 0) {
                    String tableName = cursor.getString(columnId);
                    tableNames.add(tableName);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return tableNames;
    }

    static final int FIELD_TYPE_NULL = 0;

    static final int FIELD_TYPE_INTEGER = 1;

    static final int FIELD_TYPE_FLOAT = 2;

    static final int FIELD_TYPE_STRING = 3;

    static final int FIELD_TYPE_BLOB = 4;

    public static String cursorFieldTypeUpperMap(int fieldType) {
        switch (fieldType) {
            case FIELD_TYPE_NULL:
                throw new RuntimeException("暂不支持空值");
            case FIELD_TYPE_BLOB:
                throw new RuntimeException("暂不支持二进制");
            case FIELD_TYPE_INTEGER:
                return "long";
            case FIELD_TYPE_FLOAT:
                return "double";
            case FIELD_TYPE_STRING:
                return "String";
        }
        throw new RuntimeException("不可能");
    }

    public static String dataTypeUpperMap(String dataType) {
        switch (dataType) {
            case "boolean":
            case "byte":
            case "int":
            case "short":
            case "long":
            case "char":
                return "long";
            case "float":
            case "double":
                return "double";
            case "String":
                return "String";
        }
        throw new RuntimeException("不可能");
    }

    public static int dataTypeToCursorDataType(String dataType) {
        switch (dataType) {
            case "boolean":
            case "byte":
            case "int":
            case "short":
            case "long":
            case "char":
                return FIELD_TYPE_INTEGER;
            case "float":
            case "double":
                return FIELD_TYPE_FLOAT;
            case "String":
                return FIELD_TYPE_STRING;
        }
        throw new RuntimeException("不可能");
    }
}
