package com.au.aulitesql.util;

import static android.database.Cursor.*;
import static com.au.aulitesql.actions.SqlUtils.SqlString.AU_LITE_LOG_FIELD_TABLE_NAME;
import static com.au.aulitesql.actions.SqlUtils.SqlString.AU_LITE_LOG_TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.util.CursorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HelpUtil {
    private static final String TAG = "AuLiteSqlHelper";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private void runOnMain(@NonNull Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public void copyDbToExternal(@NonNull Context context, @NonNull String dbName) {
        var dbFile =  context.getFilesDir().getPath() + "/" + context.getApplicationInfo().packageName + "/databases/" + dbName + ".db";
        var tarFile = context.getExternalCacheDir() + "/" + dbName + ".db";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.copy(Paths.get(dbFile), Paths.get(tarFile));
                runOnMain(()->{
                    Toast.makeText(context, "copy success!", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                runOnMain(()->{
                    Toast.makeText(context, "copy failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    public void printAuLiteSql() {
        var sql = AuLiteSql.sSqlHelper;
        if (sql != null) {
            var db = sql.getReadableDatabase();
            Log.d(TAG, "print AuLiteSql: Find all tabNames in <" + AU_LITE_LOG_TABLE_NAME + ">");
            var cursor = db.rawQuery("select * from " + AU_LITE_LOG_TABLE_NAME, null);
            var tabNames = new ArrayList<String>();
            int[] tableNameColumnIndex = {-1};

            if (cursor != null) {
                Log.d(TAG, "print AuLiteSql: count:" + cursor.getCount());
                CursorUtil.iterateCursor(cursor, (itemCursor)->{
                    if (tableNameColumnIndex[0] == -1) {
                        tableNameColumnIndex[0] = cursor.getColumnIndex(AU_LITE_LOG_FIELD_TABLE_NAME);
                    }
                    var name = cursor.getString(tableNameColumnIndex[0]);
                    tabNames.add(name);
                    Log.d(TAG, "print AuLiteSql: name:" + name);
                    return false;
                });
                cursor.close();
            }

            tabNames.forEach(tabName -> printSqliteDb(sql.getReadableDatabase(), tabName));
        }
    }

    public void printSqliteDb(SQLiteDatabase db, @NonNull String tableName) {
        var cursor = db.rawQuery("select * from " + tableName, null);

        if (cursor != null) {
            CursorUtil.iterateCursor(cursor, (itemCursor)->{
                var columnNames = itemCursor.getColumnNames();
                for (var columnName : columnNames) {
                    var columnIndex = itemCursor.getColumnIndex(columnName);
                    var type = itemCursor.getType(columnIndex);
                    String value = null;
                    switch (type) {
                        case FIELD_TYPE_INTEGER -> value = String.valueOf(itemCursor.getInt(columnIndex));
                        case FIELD_TYPE_FLOAT -> value = String.valueOf(itemCursor.getDouble(columnIndex));
                        case FIELD_TYPE_STRING -> value = itemCursor.getString(columnIndex);
                        case FIELD_TYPE_BLOB -> {
                            var blob = itemCursor.getBlob(columnIndex);
                            if (blob != null) {
                                var len = blob.length;
                                var sb = new StringBuilder();
                                for (var b : blob) {
                                    if (len++ < 10) {
                                        sb.append(b).append(',');
                                    } else {
                                        break;
                                    }
                                }
                                value = sb.toString();
                            }
                        }
                        case FIELD_TYPE_NULL -> {
                        }
                    }

                    Log.d(TAG, "print SqliteDb: <" + tableName + ">: " + columnName + " --> " + value);
                }
                return false;
            });

            cursor.close();
        }
    }

}
