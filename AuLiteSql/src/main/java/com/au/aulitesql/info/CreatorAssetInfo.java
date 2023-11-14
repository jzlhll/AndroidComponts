package com.au.aulitesql.info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.EntityTable;
import com.au.aulitesql.TableCreators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreatorAssetInfo {
    @NonNull
    public final ArrayList<TableAssetInfo> tableInfoList = new ArrayList<>();

    public List<String> allTableNames() {
        return tableInfoList.stream().map(tableInfo -> tableInfo.name).collect(Collectors.toList());
    }

    @Nullable
    public TableAssetInfo getByTableName(@NonNull String name) {
        for (TableAssetInfo tableInfo : tableInfoList) {
            if (tableInfo.name.equals(name)) {
                return tableInfo;
            }
        }
        return null;
    }

    /**
     * 通过直接解析注解类得到altName。再获取匹配
     */
    public TableAssetInfo getByTableClass(@NonNull Class<? extends EntityTable> clazz) {
        String name = TableCreators.tableNameFromClazz(clazz);

        for (TableAssetInfo tableInfo : tableInfoList) {
            if (tableInfo.name.equals(name)) {
                return tableInfo;
            }
        }
        return null;
    }

    /**
     * 保存成String
     * 一行名字，一行table sql创建语句。
     */
    public String saveToString() {
        StringBuilder sb = new StringBuilder();
        tableInfoList.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 != null && o2 == null) {
                return 1;
            }

            if (o1 == null) {
                return -1;
            }

            return o1.name.compareTo(o2.name);
        });

        String enter = "\n";
        for (TableAssetInfo tableInfo : tableInfoList) {
            sb.append(tableInfo.name).append(enter);
            sb.append(tableInfo.toSave()).append(enter);
        }

        return sb.toString();
    }
}