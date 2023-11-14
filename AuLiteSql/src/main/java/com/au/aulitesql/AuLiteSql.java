package com.au.aulitesql;

import static com.au.aulitesql.TableCreators.tableNameFromClazz;

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
    public <T extends EntityTable> void saveAllData(List<T> data) {
        
    }

    //随便传入一个空对象即可
    @NonNull
    public <T extends EntityTable> List<T> readAllData(@NonNull T instance) {
        var tClass = instance.getClass();
        var name = tableNameFromClazz(tClass);
        var sqlHelper = AuLiteSqliteHelper.sSqlHelper;
        if (sqlHelper != null) {
            var cursor = sqlHelper.getReadableDatabase().rawQuery("select * from " + name, null);
            var list = new ArrayList<T>();
            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    list.add(instance.fromOneLineCursor(cursor));
                    cursor.moveToNext();
                }
                cursor.close();

                return list;
            }
        } else {
            throw new RuntimeException("not init AuLiteSqliteHelper!");
        }
        return Collections.emptyList();
    }

    ////////

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

    public static void delete() {
        instance = null;
    }

    public static AuLiteSql setTableClasses(List<Class<? extends EntityTable>> tables) {
        getInstance().allTabs = tables;
        return instance;
    }

    public static AuLiteSql debugPrintAsset(List<Class<? extends EntityTable>> tables) {
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
