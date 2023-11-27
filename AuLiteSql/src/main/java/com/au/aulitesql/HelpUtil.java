package com.au.aulitesql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class HelpUtil {
//    public void copyDbToExternal(@NonNull Context context, @NonNull String dbName) {
//        var dbFile = "/data/data/" + context.getApplicationInfo().packageName + "/databases/" + dbName + ".db";
//        var targetFile =
//    }
//
//    public void printAuLiteSql(String dbName) {
//        var sql = AuLiteSql.sSqlHelper;
//        if (sql != null) {
//            var db = sql.getReadableDatabase();
//            db.execSQL("select * from " + dbName);
//            printSqliteDb(sql.getReadableDatabase());
//        }
//    }
//
//    public void printSqliteDb(SQLiteDatabase db, @NonNull String tableName) {
//        var cursor = db.rawQuery("select * from kotleokhttpcachetable", null);
//        while (cursor.moveToNext()) {
//            var indexUrl = cursor.getColumnIndex("url");
//            var indexContent = cursor.getColumnIndex("content");
//            var index3 = cursor.getColumnIndex("region");
//            var index4 = cursor.getColumnIndex("lang");
//            var index5 = cursor.getColumnIndex("params");
//            if (indexUrl >= 0 && indexContent >= 0 && index3 >= 0 && index4 >= 0 && index5 >= 0) {
//                android.util.Log.d(
//                        "typhur_app",
//                        "db: region= ${cursor.getString(index3)}|${cursor.getString(index4)}"
//                                + " ,url= " + cursor.getString(indexUrl)
//                                + " ,params= " + cursor.getString(index5)
//                                + ", " + cursor.getString(indexContent)
//                )
//            } else {
//                android.util.Log.d("typhur_app", "db: error $indexUrl, $indexContent, $index3, $index4, $index5")
//            }
//        }
//        cursor.close()
//    }

}
