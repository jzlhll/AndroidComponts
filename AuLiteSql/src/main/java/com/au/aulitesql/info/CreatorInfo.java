package com.au.aulitesql.info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.EntityTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CreatorInfo {
    public CreatorInfo(List<Class<? extends EntityTable>> entityTables) {
        this.entityTables = entityTables;
    }

    @NonNull
    public final ArrayList<TableInfo> tableInfoList = new ArrayList<>();

    @NonNull
    public final List<Class<? extends EntityTable>> entityTables;

    public List<String> allTableNames() {
        return tableInfoList.stream().map((Function<TableInfo, String>) tableInfo -> tableInfo.name).collect(Collectors.toList());
    }

    @Nullable
    public TableInfo getByTableName(@NonNull String name) {
        for (TableInfo tableInfo : tableInfoList) {
            if (tableInfo.name.equals(name)) {
                return tableInfo;
            }
        }
        return null;
    }

    @Nullable
    public TableInfo getByTableClass(@NonNull Class<? extends EntityTable> clazz) {
        for (TableInfo tableInfo : tableInfoList) {
            if (tableInfo.entityTable.equals(clazz)) {
                return tableInfo;
            }
        }
        return null;
    }
}