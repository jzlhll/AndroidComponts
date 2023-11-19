package com.au.aulitesql;

import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public interface IDao {
    <E extends Entity> List<E> loadAllFilter(Class<E> clazz, String fieldName, Object value);
    <E extends Entity> List<E> loadAll(Class<E> clazz);
    <E extends Entity> List<E> loadAll(Class<E> clazz, @NonNull String fieldName, Object value,
                                       String groupBy, String having, String orderBy);

    <E extends Entity> List<E> loadAll(Class<E> clazz, @NonNull String selections, @NonNull String[] selectionArgs,
                                       String groupBy, String having, String orderBy);

    <E extends Entity> List<E> rawLoadAll(Class<E> clazz, String sql, String[] selectionArgs);

    //////////////////////////////////
    //////////////////////////////////

    <E extends Entity> int deleteAll(List<E> dataList);

    boolean delete(Entity instance);

    //////////////////////////////////
    //////////////////////////////////

    Entity save(Entity instance);

    /**
     * @param status 请传入 new boolean[] {false}。用来接收插入是否成功结果。
     */
    Entity save(Entity instance, @NonNull boolean[] status);
    <E extends Entity> int saveAll(List<E> dataList);

    //////////////////////////////////
    //////////////////////////////////

    //cursor没有关闭。交给调用者关闭。谁打开谁关闭。
    @NonNull
    static <T extends Entity> List<T> cursorToData(Cursor cursor, Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        var list = new ArrayList<T>();
        var constructors = clazz.getConstructors(); //later 要求必须有空构造函数。
        Constructor<?> constructor = null;
        if (constructors.length > 0) {
            constructor = constructors[0];
        }
        if (cursor != null && cursor.moveToFirst() && constructor != null) {
            while (!cursor.isAfterLast()) {
                T data = (T) constructor.newInstance();
                var columnID_id = cursor.getColumnIndex(BaseColumns._ID);
                if (columnID_id >= 0) data.setId(cursor.getLong(columnID_id));
                data.unpack(cursor);
                list.add(data);
                cursor.moveToNext();
            }
        }
        return list;
    }
}