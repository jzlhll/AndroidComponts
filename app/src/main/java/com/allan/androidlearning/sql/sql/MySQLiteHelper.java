package com.allan.androidlearning.sql.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "news";

    public static final String CREATE_NEWS = "create table " + TABLE_NAME + " ("
            + "id integer primary key autoincrement, "
            + "title text, "
            + "content text, "
            + "publishdate integer,"
            + "commentcount integer)";

    public static final int VERSION_V1 = 1;
    private static final String DB_NAME = "mysql.db";

    public MySQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION_V1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
