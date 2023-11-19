package com.au.aulitesql;

import static com.au.aulitesql.AuLiteSql.tableNameFromClazz;
import static com.au.aulitesql.EntityTable._ID_WHERE_CAUSE;

import android.content.ContentValues;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * @author allan.jiang
 * @date :2023/11/15 17:14
 */
public class DefaultDao implements IDao{

    @Override
    @NonNull
    public <E extends EntityTable> List<E> loadAll(Class<E> clazz) {
        var name = tableNameFromClazz(clazz);
        var sqlHelper = AuLiteSql.sSqlHelper;
        if (sqlHelper != null) {
            var cursor = sqlHelper.getReadableDatabase().rawQuery("select * from " + name, null);
            List<E> list;
            try {
                list = IDao.cursorToData(cursor, clazz);
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

    @Override
    public <E extends EntityTable> List<E> loadAllFilter(Class<E> clazz, String fieldName, Object fieldValue) {
        return loadAll(clazz, fieldName, fieldValue, null, null, null);
    }

    /**
     * 根据某个字段查询结果。
     */
    @Override
    public <E extends EntityTable> List<E> loadAll(Class<E> clazz, @NonNull String fieldName, Object value,
                                                      String groupBy, String having, String orderBy) {
        return loadAll(clazz, fieldName + "= ?", new String[] {String.valueOf(value)}, groupBy, having, orderBy);
    }

    /**
     * 根据 标准接口，某个字段查询结果。
     */
    @Override
    public <E extends EntityTable> List<E> loadAll(Class<E> clazz, @NonNull String selection, @NonNull String[] selectionArgs,
                                                          String groupBy, String having, String orderBy) {
        var sqlHelper = AuLiteSql.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getReadableDatabase();
            var tableName = tableNameFromClazz(clazz);
            var cursor = db.query(tableName, null, selection, selectionArgs, groupBy, having, orderBy);
            List<E> list;
            try {
                list = IDao.cursorToData(cursor, clazz);
            }catch (Exception e) {
                e.printStackTrace();
                list = Collections.emptyList();
            }
            cursor.close();
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public <E extends EntityTable> List<E> rawLoadAll(Class<E> clazz, String sql, String[] selectionArgs) {
        var sqlHelper = AuLiteSql.sSqlHelper;
        if (sqlHelper != null) {
            var db = sqlHelper.getReadableDatabase();
            var cursor = db.rawQuery(sql, selectionArgs);
            List<E> list;
            try {
                list = IDao.cursorToData(cursor, clazz);
            }catch (Exception e) {
                e.printStackTrace();
                list = Collections.emptyList();
            }
            cursor.close();
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public <E extends EntityTable> int deleteAll(List<E> dataList) {
        var sqlHelper = AuLiteSql.sSqlHelper;
        int count = 0;
        if (sqlHelper != null) {
            var db = sqlHelper.getWritableDatabase();
            db.beginTransaction();
            for (var instance : dataList) {
                var tableName = tableNameFromClazz(instance.getClass());
                count += db.delete(tableName, _ID_WHERE_CAUSE, new String[] {String.valueOf(instance.getId())});
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return count;
    }

    @Override
    public boolean delete(EntityTable instance) {
        var sqlHelper = AuLiteSql.sSqlHelper;
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

    @Override
    public EntityTable save(EntityTable instance) {
        return save(instance, new boolean[]{false});
    }

    /**
     * @param status 请传入 new boolean[] {false}。用来接收插入是否成功结果。
     */
    @Override
    public EntityTable save(EntityTable instance, @NonNull boolean[] status) {
        var sqlHelper = AuLiteSql.sSqlHelper;
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

    @Override
    public <E extends EntityTable> int saveAll(List<E> dataList) {
        var sqlHelper = AuLiteSql.sSqlHelper;
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

}