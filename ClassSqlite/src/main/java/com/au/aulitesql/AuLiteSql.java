package com.au.aulitesql;

import static com.au.aulitesql.EntityTable._ID_WHERE_CAUSE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.au.aulitesql.annotation.AuName;
import com.au.aulitesql.info.NamesPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class AuLiteSql {
    private AuLiteSql() {}
    public static AuLiteSql getInstance() {
        if (instance == null) {
            synchronized (AuLiteSql.class) {
                if (instance == null) {
                    instance = new AuLiteSql();
                }
            }
        }
        return instance;
    }

    static void destroy() {
        instance = null;
    }

    public static String tableNameFromClazz(Class<? extends EntityTable> clazz) {
        var name = clazz.getSimpleName();
        //1. 替代tableName解析
        if (clazz.isAnnotationPresent(AuName.class)) {
            AuName annotation = clazz.getAnnotation(AuName.class);
            if (annotation != null) {
                String value = annotation.value();
                if (value != null && !value.isEmpty()) {
                    name = value;
                }
            }
        }
        return name;
    }

    /**
     * 根据某个字段查询结果。
     */
    public static <T extends EntityTable, P> List<T> loadAllData(Class<T> clazz, @NonNull String fieldName, P value,
                                                        String groupBy, String having, String orderBy) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getReadableDatabase();
            var tableName = tableNameFromClazz(clazz);
            var cursor = db.query(tableName, null, fieldName + "=?", new String[] {String.valueOf(value)}, groupBy, having, orderBy);
            List<T> list;
            try {
                list = cursorToData(cursor, clazz);
            }catch (Exception e) {
                e.printStackTrace();
                list = Collections.emptyList();
            }
            cursor.close();
            return list;
        }
        return Collections.emptyList();
    }

    public static <T extends EntityTable> boolean deleteAllData(List<T> dataList) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            db.beginTransaction();
            for (var instance : dataList) {
                var tableName = tableNameFromClazz(instance.getClass());
                db.delete(tableName, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        }
        return false;
    }

    public static boolean deleteData(EntityTable instance) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            var tableName = tableNameFromClazz(instance.getClass());
            if (instance.getId() >= 0) {
                return db.delete(tableName, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())}) >= 0;
            } else {
                return true;
            }
        }
        return false;
    }

    public static EntityTable saveData(EntityTable instance) {
        return saveData(instance, new boolean[]{false});
    }

    /**
     * @param status 请传入 new boolean[] {false}。用来接收插入是否成功结果。
     */
    public static EntityTable saveData(EntityTable instance, @NonNull boolean[] status) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            status[0] = true;
            var db = sqlHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            instance.pack(cv);
            var tableName = tableNameFromClazz(instance.getClass());
            if (instance.getId() >= 0) {
                db.update(tableName, cv, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
                return instance;
            } else {
                var r = db.insert(tableName, null, cv);
                if (r == -1) {
                    Log.e("AuLiteSql", "error when saveData " + instance);
                    status[0] = false;
                }
                instance.setId(r);
                return instance;
            }
        }
        status[0] = false;
        return null;
    }

    public static <T extends EntityTable> int saveAllData(List<T> dataList) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        var successSize = 0;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            db.beginTransaction();
            for (var instance : dataList) {
                ContentValues cv = new ContentValues();
                instance.pack(cv);
                var tableName = tableNameFromClazz(instance.getClass());
                if (instance.getId() >= 0) {
                    var r = db.update(tableName, cv, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
                    if(r > 0) successSize += r;
                } else {
                    var r = db.insert(tableName, null, cv);
                    if (r == -1) {
                        Log.e("AuLiteSql", "error when saveData " + instance);
                    } else {
                        successSize++;
                    }
                    instance.setId(r);
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return successSize;
    }

    //随便传入一个空对象即可
    @NonNull
    public static <T extends EntityTable> List<T> loadAllData(Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        var name = tableNameFromClazz(clazz);
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var cursor = sqlHelper.getReadableDatabase().rawQuery("select * from " + name, null);
            List<T> list;
            try {
                list = cursorToData(cursor, clazz);
            } catch (Exception e) {
                e.printStackTrace();
                list = Collections.emptyList();
            }
            cursor.close();
            return list;
        } else {
            throw new RuntimeException("not init AuLiteSqliteHelper!");
        }
    }

    //cursor没有关闭。交给调用者关闭。谁打开谁关闭。
    public static <T extends EntityTable> List<T> cursorToData(Cursor cursor, Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        var list = new ArrayList<T>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                T data = (T) clazz.getConstructors()[0].newInstance(); //later 要求必须有空构造函数。
                var columnID_id = cursor.getColumnIndex(BaseColumns._ID);
                if (columnID_id >= 0) data.setId(cursor.getLong(columnID_id));
                data.unpack(cursor);
                list.add(data);
                cursor.moveToNext();
            }
        }
        return list;
    }

    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////

    @NonNull
    public static Gson getGsonOrNew() {
        if (instance != null && instance.gson != null) {
            return instance.gson;
        }
        instance.gson = new GsonBuilder().create();
        return instance.gson;
    }

    private static volatile AuLiteSql instance;
    private Gson gson;
    int dbVersion = 1;
    String dbName = "au_sqlite.db";
    String assetFile;
    @NonNull
    final AuLiteAssetAutoMigrations migrations = new AuLiteAssetAutoMigrations();

    List<Class<? extends EntityTable>> currentAllTabs;

    public AuLiteSql setDb(@NonNull String dbName, int dbVersion) {
        this.dbName = dbName;
        this.dbVersion = dbVersion;
        return this;
    }

    /**
     * 你可以配置你全局的gson进来。在初始化阶段。
     * 否则，就将自行创建。
     */
    public AuLiteSql setGson(@NonNull Gson gson) {
        this.gson = gson;
        return this;
    }

    public AuLiteSql setTableClasses(List<Class<? extends EntityTable>> tables) {
        currentAllTabs = tables;
        return this;
    }

    public AuLiteSql setAsset(String assetFile) {
        this.assetFile = assetFile;
        return this;
    }

    /**
     *  设置表内有变化的情况进行内部迁移。
     *  （其实我是改了表名，然后新建同名新表，进行数据迁移）
     *  如果不设置该函数，则表内迁移，只会保留字段不变的内容。其他全部删除。
     */
    public AuLiteSql setTabInnerMigrations(Map<Class<? extends EntityTable>, IManualMigration> tableInners) {
        migrations.tableInnersMigrations = tableInners;
        return this;
    }

    /**
     *  设置从某张老表，迁移到新表的方法。
     *  如果不设置该函数，则老表，如果不存在了，则会被直接删除。
     *  数据不会被自动迁移，因为不清楚映射关系。
     *
     *  Map的key是Pair<老Table名字, 新Class名>，因为老表名对应的class可能已经删除，
     *  所以只能你自行传入String(如果有通过AuAltName注解的则也应该使用AltName）
     *
     *  暂不支持，过于复杂的转换。比如，我就是存在某个同名老表，但是我就想将某个老表1，数据迁移到老表2中去。
     */
    public AuLiteSql setTabsMigrations(Map<NamesPair, IManualMigration> tables) {
        migrations.tablesMigrations = tables;
        return this;
    }

    /////////////////////////////////////
    /////////////////////////////////////
    /////////////////////////////////////
    public AuLiteSqliteHelper start(Context applicationContext) {
        return new AuLiteSqliteHelper(applicationContext, dbName, null, dbVersion);
    }
}
