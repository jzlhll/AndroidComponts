package com.au.aulitesql;

import android.content.ContentValues;
import android.database.Cursor;

import com.au.aulitesql.info.Pair;

public interface IManualMigration {
    /**
     * @param newTable 即你新的类名
     * @param oldTableDataCursor 老的该类名Table的所有数据。你不需要关闭，我会帮你关闭。
     *
     * @return 返回值，List就是你迁移后的新数据列表，我会帮你插入到新表中；Bool表示你是否要留存老表，如果是同名表自然无效。
     */
    Pair<ContentValues, Boolean> onTableInnerMigrate(Class<? extends Entity> newTable, Cursor oldTableDataCursor);
}