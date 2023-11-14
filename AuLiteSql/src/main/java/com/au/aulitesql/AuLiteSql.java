package com.au.aulitesql;

import static com.au.aulitesql.EntityTable._ID_WHERE_CAUSE;
import static com.au.aulitesql.TableCreators.tableNameFromClazz;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.au.aulitesql.info.CreatorAssetInfo;
import com.au.aulitesql.info.NamesPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuLiteSql {
    /**
     * 根据某个字段查询结果。
     */
    public static <T extends EntityTable, P> List<T> getAllFilter(Class<T> clazz, @NonNull String fieldName, P value,
                                                        String groupBy, String having, String orderBy) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getReadableDatabase();
            var tableName = tableNameFromClazz(clazz);
            var cursor = db.query(tableName, null, fieldName + "=?", new String[] {fieldName}, groupBy, having, orderBy);
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

    public static <T extends EntityTable> boolean deleteList(List<T> dataList) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            db.beginTransaction();
            for (var instance : dataList) {
                ContentValues cv = new ContentValues();
                instance.prepareDbData(cv);
                var tableName = tableNameFromClazz(instance.getClass());
                db.delete(tableName, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        }
        return false;
    }

    static boolean deleteData(EntityTable instance) {
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

    static long saveData(EntityTable instance) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            instance.prepareDbData(cv);
            var tableName = tableNameFromClazz(instance.getClass());
            if (instance.getId() >= 0) {
                db.update(tableName, cv, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
                return instance.getId();
            } else {
                return db.insert(tableName, null, cv);
            }
        }
        return -1;
    }

    public <T extends EntityTable> boolean saveAllData(List<T> dataList) {
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            db.beginTransaction();
            for (var instance : dataList) {
                ContentValues cv = new ContentValues();
                instance.prepareDbData(cv);
                var tableName = tableNameFromClazz(instance.getClass());
                if (instance.getId() >= 0) {
                    db.update(tableName, cv, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
                } else {
                    db.insert(tableName, null, cv);
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        }
        return false;
    }

    //随便传入一个空对象即可
    @NonNull
    public <T extends EntityTable> List<T> loadAllData(Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
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

    //cursor没有关闭。
    public static <T extends EntityTable> List<T> cursorToData(Cursor cursor, Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        var list = new ArrayList<T>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                var columns = cursor.getColumnNames();

                T data = (T) clazz.getConstructors()[0].newInstance();

                for (var columnName : columns) {
                    var columnIndex = cursor.getColumnIndex(columnName);
                    if (columnIndex >= 0) {
                        if (columnName.equals(BaseColumns._ID)) {
                            data.setId(cursor.getLong(columnIndex));
                        } else {
                            data.setFieldFromDbCursor(cursor, columnIndex, columnName);
                        }
                    }
                }
                list.add(data);
                cursor.moveToNext();
            }
        }
        return list;
    }
    //////////////////////////////////////////////

    private AuLiteSql() {}

    private static AuLiteSql instance;
    static boolean isDebugPrintAsset = false;

    List<Class<? extends EntityTable>> allTabs;

    public static AuLiteSql getInstance() {
        if (instance == null) {
            instance = new AuLiteSql();
        }
        return instance;
    }

    private Gson gson;

    /**
     * 你可以配置你全局的gson进来。在初始化阶段。
     * 否则，就将自行创建。
     * @param gson
     */
    public static AuLiteSql initGson(@NonNull Gson gson) {
        getInstance().gson = gson;
        return instance;
    }

    public static Gson getGsonOrNew() {
        if (instance != null && instance.gson != null) {
            return instance.gson;
        }
        return new GsonBuilder().create();
    }

    int dbVersion = 1;
    String dbName = "au_sqlite.db";

    public static AuLiteSql initDb(@NonNull String dbName, int dbVersion) {
        getInstance().dbName = dbName;
        instance.dbVersion = dbVersion;
        return instance;
    }

    @NonNull
    final AuLiteAssetAutoMigrations migrations = new AuLiteAssetAutoMigrations();

    public static AuLiteSql setTableClasses(List<Class<? extends EntityTable>> tables) {
        getInstance().allTabs = tables;
        return instance;
    }

    public static AuLiteSql initDebugPrintAsset(List<Class<? extends EntityTable>> tables) {
        isDebugPrintAsset = true;
        getInstance().allTabs = tables;
        CreatorAssetInfo creatorInfo = null;
        try {
            creatorInfo = new TableCreators(tables).collect();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        if (creatorInfo != null) {
            Log.w("AuLiteSql", "Please save to asset....to > auLiteSql/dbCreator.txt");
            Log.w("AuLiteSql", creatorInfo.saveToString());
            Log.w("AuLiteSql", "Please save to asset....end!");
        }
        return instance;
    }

    public static void setAsset(String assetFile) {
        AuLiteSqliteHelper.assetFile = assetFile;
    }

    /**
     *  设置表内有变化的情况进行内部迁移。
     *  （其实我是改了表名，然后新建同名新表，进行数据迁移）
     *  如果不设置该函数，则表内迁移，只会保留字段不变的内容。其他全部删除。
     */
    public static AuLiteSql setTabInnerMigrations(Map<Class<? extends EntityTable>, IManualMigration> tableInners) {
        AuLiteSql.getInstance().migrations.tableInnersMigrations = tableInners;
        return instance;
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
    public static AuLiteSql setTabsMigrations(Map<NamesPair, IManualMigration> tables) {
        AuLiteSql.getInstance().migrations.tablesMigrations = tables;
        return instance;
    }
}
