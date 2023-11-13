package com.au.aulitesql;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.info.CreatorInfo;
import com.au.aulitesql.info.TableInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class AuLiteSqliteHelper extends SQLiteOpenHelper {
    static int dbVersion = 1;
    static String dbName = "au_sqlite.db";

    static CreatorInfo creatorInfo;
    static final AuLiteAutoMigrations migrations = new AuLiteAutoMigrations();

    public static void init(List<Class<? extends EntityTable>> tables) {
        init("au_sqlite.db", 1, tables);
    }

    public static void init(@NonNull String dbName, int dbVersion,
                            List<Class<? extends EntityTable>> tables) {
        AuLiteSqliteHelper.dbName = dbName;
        AuLiteSqliteHelper.dbVersion = dbVersion;
        try {
            creatorInfo = new TableCreators(tables).collect();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     *  设置表内有变化的情况进行内部迁移。
     *  （其实我是改了表名，然后新建同名新表，进行数据迁移）
     *  如果不设置该函数，则表内迁移，只会保留字段不变的内容。其他全部删除。
     */
    public static void setTabInnerMigrations(List<IManualMigration<? extends EntityTable>> tableInners) {
        migrations.tableInnersMigrations = tableInners;
    }

    /**
     *  设置从某张老表，迁移到新表的方法。
     *  如果不设置该函数，则老表，如果不存在了，则会被直接删除。
     *  数据不会被自动迁移，因为不清楚映射关系。
     *
     *  Map的key是Pair<老Table名字, 新Class名>，因为老表名可能已经删除，所以只能你自行传入String，而且如果有通过AuAltName注解的，
     *  则也应该使用AltName为准。
     *
     *  暂不支持，过于复杂的转换。比如，我就是存在某个同名老表，但是我就想将某个老表1，数据迁移到老表2中去。
     */
    public static void setTabsMigrations(Map<Pair<String, Class<? extends EntityTable>>,
            IManualMigration<? extends EntityTable>> tables) {
        migrations.tablesMigrations = tables;
    }

    public AuLiteSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AuLiteSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CreatorInfo ci = creatorInfo;
        if (ci != null) {
            for (TableInfo ti : ci.tableInfoList) {
                db.execSQL(ti.sql);
            }
        }
        db.execSQL(SqlUtils.SqlString.sqlCreateLogTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        migrations.onVersionChange(creatorInfo, db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        migrations.onVersionChange(creatorInfo, db);
    }
}
