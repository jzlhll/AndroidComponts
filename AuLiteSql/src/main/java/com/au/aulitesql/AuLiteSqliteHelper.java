package com.au.aulitesql;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.au.aulitesql.actions.SqlUtils;
import com.au.aulitesql.actions.TableCreators;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AuLiteSqliteHelper extends SQLiteOpenHelper {

    private final Context context;

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
        //创建日志表
        db.execSQL(SqlUtils.SqlString.sqlCreateLogTable);

        //从asset文件中解析sql创建语句。
        //var creators = creatorStringFromAssetFile();
        //从collect() 结果中获取 sql创建语句。
        var creators = AuLiteSql.getInstance().getCreateInfo().tableInfoList;

        for (int i = 0, size = creators.size(); i < size; i++) {
            var tableInfo = creators.get(i);
            Log.d(AuLiteSql.TAG, "onCreate Db Str: " + tableInfo.name + ",, " + tableInfo.sql);
            //创建表
            db.execSQL(tableInfo.sql);
            //写入日志表，创建信息
            var logCv = new ContentValues();
            logCv.put(SqlUtils.SqlString.AU_LITE_LOG_FIELD_CREATE_SQL, tableInfo.sql);
            logCv.put(SqlUtils.SqlString.AU_LITE_LOG_FIELD_TABLE_NAME, tableInfo.name);
            db.insert(SqlUtils.SqlString.AU_LITE_LOG_TABLE_NAME, null, logCv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AuLiteSql.getInstance().migrations.onVersionChange(AuLiteSql.getInstance().getCreateInfo(), db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * @return 第0行是名字，第1行是sqlCreate语句。第2行是第二个名字，第三行是sqlCreate语句。以此类推。
     */
    private String[] creatorStringFromAssetFile() {
        List<String> r = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(AuLiteSql.getInstance().assetFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                r.add(line);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r.toArray(new String[]{});
    }
}