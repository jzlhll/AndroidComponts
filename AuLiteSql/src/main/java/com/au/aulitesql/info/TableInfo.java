package com.au.aulitesql.info;


import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.Entity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
    public transient Class<? extends Entity> entityTable;

    public String className;

    public String name;

    public final List<FieldInfo> fieldInfoList = new ArrayList<>();

    public String sql;

    public String toSave() {
        Gson gson = AuLiteSql.getGsonOrNew();
        return gson.toJson(this);
    }

    public static TableInfo fromSave(@NonNull String save) {
        Gson gson = AuLiteSql.getGsonOrNew();
        return gson.fromJson(save, TableInfo.class);
    }
}