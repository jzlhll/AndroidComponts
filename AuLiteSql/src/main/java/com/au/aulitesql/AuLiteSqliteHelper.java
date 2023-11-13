package com.au.aulitesql;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AuLiteSqliteHelper extends SQLiteOpenHelper {
    public static String assetFile;

    private final Context context;

    /**
     * @return 第0行是名字，第1行是sqlCreate语句。第2行是第二个名字，第三行是sqlCreate语句。以此类推。
     */
    private List<String> creatorStringFromAssetFile() {
        List<String> r = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(assetFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                r.add(line);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public AuLiteSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public AuLiteSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (AuLiteSql.isDebugPrintAsset) {
//            CreatorInfo ci = AuLiteSql.getInstance().creatorInfo;
//            if (ci != null) {
//                for (TableInfo ti : ci.tableInfoList) {
//                    db.execSQL(ti.sql);
//                }
//            }
            //db.execSQL(SqlUtils.SqlString.sqlCreateLogTable);
        } else {
            //从asset文件中解析sql创建语句。
            //创建日志表
            db.execSQL(SqlUtils.SqlString.sqlCreateLogTable);
            List<String> creators = creatorStringFromAssetFile();
            for (int i = 0, size = creators.size(); i < size; i+=2) {
                //创建表
                db.execSQL(creators.get(i+1));
                //写入日志表，创建信息
                var logCv = new ContentValues();
                logCv.put(SqlUtils.SqlString.AU_LITE_LOG_CREATE_SQL, creators.get(i));
                db.insert(SqlUtils.SqlString.AU_LITE_LOG_TABLE_NAME, null, logCv);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AuLiteSql.getInstance().migrations.onVersionChange(AuLiteSql.getInstance().creatorInfo, db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AuLiteSql.getInstance().migrations.onVersionChange(AuLiteSql.getInstance().creatorInfo, db);
    }
}