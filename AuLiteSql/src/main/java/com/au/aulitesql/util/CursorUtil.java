package com.au.aulitesql.util;

import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.au.aulitesql.Entity;
import com.au.aulitesql.actions.ICallback;
import com.au.aulitesql.info.UnImplmentionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CursorUtil {
    public interface ItemCursorProcessor {
        /**
         * @return 返回true，表示不再继续处理。
         */
        boolean process(Cursor cursor);
    }

    public interface OnlyOnceItemCursorProcessor {
        /**
         * @return 返回true，表示不再继续处理。
         */
        void process(Cursor cursor);
    }

    /**
     * 固定的cursor处理流程。
     * 我们只需要处理每一项的获取即可。
     * 交给调用者关闭，谁打开谁关闭。
     */
    public static void iterateCursor(@NonNull Cursor cursor, @NonNull ItemCursorProcessor itemProcessor) {
        iterateCursor(cursor, itemProcessor, null);
    }

    public static void iterateCursor(@NonNull Cursor cursor, ItemCursorProcessor itemProcessor, OnlyOnceItemCursorProcessor onlyOnceItemCursorProcessor) {
        if (cursor.moveToFirst()) {
            if (!cursor.isAfterLast()) {
                onlyOnceItemCursorProcessor.process(cursor);
            }

            while (!cursor.isAfterLast()) {
                if (itemProcessor.process(cursor)) {
                    break;
                }
                cursor.moveToNext();
            }
        }
    }

    //cursor没有关闭。交给调用者关闭。谁打开谁关闭。
    @NonNull
    public static <T extends Entity> List<T> cursorToData(Cursor cursor, Class<T> clazz) throws InvocationTargetException,
            IllegalAccessException, InstantiationException, UnImplmentionException {
        var list = new ArrayList<T>();
        var constructors = clazz.getConstructors(); //later 要求必须有空构造函数。
        Constructor<?> constructor = null;
        if (constructors.length > 0) {
            constructor = constructors[0];
        }
        if (cursor != null && cursor.moveToFirst() && constructor != null) {
            while (!cursor.isAfterLast()) {
                T data = (T) constructor.newInstance();
                data.unpack(cursor);
                list.add(data);
                cursor.moveToNext();
            }
        }
        return list;
    }

}
