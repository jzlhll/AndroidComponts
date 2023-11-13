package com.au.aulitesql.info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.EntityTable;

import java.util.List;

public class CreatorInfo extends CreatorAssetInfo {
    public CreatorInfo(@NonNull List<Class<? extends EntityTable>> entityTables) {
        this.entityTables = entityTables;
    }

    public final transient List<Class<? extends EntityTable>> entityTables;

    @Nullable
    @Override
    public TableAssetInfo getByTableClass(@NonNull Class<? extends EntityTable> clazz) {
        for (TableAssetInfo tableInfo : tableInfoList) {
            if (tableInfo instanceof TableInfo && ((TableInfo) tableInfo).entityTable.equals(clazz)) {
                return tableInfo;
            }
        }
        return null;
    }
}