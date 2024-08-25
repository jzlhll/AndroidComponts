package com.au.aulitesql;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.au.aulitesql.actions.AuLiteAssetAutoMigrations;
import com.au.aulitesql.actions.DefaultDao;
import com.au.aulitesql.actions.TableCreators;
import com.au.aulitesql.annotation.AuName;
import com.au.aulitesql.executor.AuLiteRunExecutor;
import com.au.aulitesql.info.NamesPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public final class AuLiteSql {
    public static final String TAG = AuLiteSql.class.getSimpleName();

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

    public synchronized AuLiteSqliteHelper openDataBase(Context cxt) {
        var sql = AuLiteSql.getInstance();
        if (sSqlHelper == null) {
            sSqlHelper = new AuLiteSqliteHelper(cxt.getApplicationContext(),
                    sql.dbName, null, sql.dbVersion);
        }
        return sSqlHelper;
    }

    public synchronized void closeDataBase() {
        var helper = sSqlHelper;
        if (helper != null) {
            synchronized (helper) {
                exec.close();
                helper.close();
            }
        }
    }

    private static final HashMap<Class<?>, String> cachedTableNameFromClazz = new HashMap<>(4);

    public static String tableNameFromClazz(Class<? extends Entity> clazz) {
        if (cachedTableNameFromClazz.containsKey(clazz)) {
            return cachedTableNameFromClazz.get(clazz);
        }
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
        cachedTableNameFromClazz.put(clazz, name);
        return name;
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

    public static AuLiteSqliteHelper sSqlHelper;

    private static volatile AuLiteSql instance;

    /**
     * 不做final，可以外部设置额外的dao。
     */
    private static IDao dao = new DefaultDao();

    public static void setDao(@NonNull IDao dao) {
        AuLiteSql.dao = dao;
    }

    @NonNull
    public static IDao getDao() {
        return dao;
    }

    private Gson gson;
    int dbVersion = 1;
    String dbName = "au_sqlite.db";
    String assetFile;
    @NonNull
    final AuLiteAssetAutoMigrations migrations = new AuLiteAssetAutoMigrations();

    private List<Class<? extends Entity>> currentAllTabs;
    private volatile TableCreators.CreatorInfo createInfo;

    @NonNull
    public TableCreators.CreatorInfo getCreateInfo() {
        try {
            if (createInfo == null) {
                synchronized (this) {
                    if (createInfo == null) {
                        createInfo = new TableCreators(currentAllTabs).collect();
                    }
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        return createInfo;
    }

    private final AuLiteRunExecutor exec = new AuLiteRunExecutor();

    public AuLiteSql useHandlerMode() {
        exec.useHandlerMode();
        return this;
    }

    public AuLiteSql useThreadPool() {
        exec.useThreadPool();
        return this;
    }

    public AuLiteSql useHandlerMode(Looper looper) {
        exec.useHandlerMode(looper);
        return this;
    }

    public AuLiteSql useHandlerMode(Handler subHandler) {
        exec.useHandlerMode(subHandler);
        return this;
    }

    public AuLiteSql useThreadPool(Executor executor) {
        exec.useThreadPool(executor);
        return this;
    }

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

    public AuLiteSql setTableClasses(List<Class<? extends Entity>> tables) {
        currentAllTabs = tables;
        return this;
    }

//    public AuLiteSql setAsset(String assetFile) {
//        this.assetFile = assetFile;
//        return this;
//    }

    /**
     *  设置表内有变化的情况进行内部迁移。
     *  （其实我是改了表名，然后新建同名新表，进行数据迁移）
     *  如果不设置该函数，则表内迁移，只会保留字段不变的内容。其他全部删除。
     */
    public AuLiteSql setTabInnerMigrations(Map<Class<? extends Entity>, IManualMigration> tableInners) {
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

    public void execute(Runnable runnable) {
        exec.execute(runnable);
    }

    /////////////////////////////////////
    /////////////////////////////////////
    /////////////////////////////////////
}
