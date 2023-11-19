package com.au.aulitesql.actions;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.au.aulitesql.info.Pair;

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

        public static final String AU_LITE_LOG_TABLE_NAME = "au_lite_log";

        public static final String AU_LITE_LOG_FIELD_CREATE_SQL = "createSql";
        public static final String AU_LITE_LOG_FIELD_TABLE_NAME = "tableName";
        public static final String sqlCreateLogTable
                = "CREATE TABLE IF NOT EXISTS " + AU_LITE_LOG_TABLE_NAME + " (" +
                AU_LITE_LOG_FIELD_TABLE_NAME + " TEXT PRIMARY KEY, \n" +
                AU_LITE_LOG_FIELD_CREATE_SQL + " TEXT, \n" +
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

    @SuppressLint("Range")
    @WorkerThread
    public static Pair<String, Long> queryCreateSqlLog(@NonNull SQLiteDatabase db, @NonNull String tabName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqlString.AU_LITE_LOG_TABLE_NAME,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String createSql = null;
            long id = -1;
            while (!cursor.isAfterLast()) {
                if (tabName.equals(cursor.getString(cursor.getColumnIndex(SqlString.AU_LITE_LOG_FIELD_TABLE_NAME)))) {
                    createSql = cursor.getString(cursor.getColumnIndex(SqlString.AU_LITE_LOG_FIELD_CREATE_SQL));
                    id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                    break;
                }
                cursor.moveToNext();
            }
            cursor.close();

            return new Pair<>(createSql, id);
        }
        return null;
    }

    @WorkerThread
    public static boolean updateCreateSqlLog(@NonNull SQLiteDatabase db, @NonNull String tabName, @NonNull String createSql) {
        var oldData = queryCreateSqlLog(db, tabName);
        var cv = new ContentValues();
        cv.put(SqlString.AU_LITE_LOG_FIELD_CREATE_SQL, createSql);
        cv.put(SqlString.AU_LITE_LOG_FIELD_TABLE_NAME, tabName);

        if (oldData != null && oldData.p2 != -1) {
            return db.update(SqlString.AU_LITE_LOG_TABLE_NAME, cv, BaseColumns._ID + " = ?",
                    new String[] {String.valueOf(oldData.p2)}) > 0;
        } else {
            return db.insert(SqlString.AU_LITE_LOG_TABLE_NAME, null, cv) >= 0;
        }
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
    static final int FIELD_TYPE_LIST = 5;
    static final int FIELD_TYPE_ARRAY_LIST = 6;
    static final int FIELD_TYPE_SET = 7;
    static final int FIELD_TYPE_HASH_SET = 8;

    static final int FIELD_TYPE_MAP = 9;
    static final int FIELD_TYPE_HASH_MAP = 10;

    public static int dataTypeToCursorDataType(String dataType) {
        switch (dataType) {
            case "boolean", "byte", "int", "short", "long", "char" -> {
                return FIELD_TYPE_INTEGER;
            }
            case "float", "double" -> {
                return FIELD_TYPE_FLOAT;
            }
            case "String" -> {
                return FIELD_TYPE_STRING;
            }
            case "List" -> {
                return FIELD_TYPE_LIST;
            }
            case "ArrayList" -> {
                return FIELD_TYPE_ARRAY_LIST;
            }
            case "Set" -> {
                return FIELD_TYPE_SET;
            }
            case "HashSet" -> {
                return FIELD_TYPE_HASH_SET;
            }
            case "Map" -> {
                return FIELD_TYPE_MAP;
            }
            case "HashMap" -> {
                return FIELD_TYPE_HASH_MAP;
            }
        }
        throw new RuntimeException("不可能");
    }
}
