package com.au.aulitesql.info;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.AuLiteSql;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TableAssetInfo {
    public String className;

    public String name;

    public final List<FieldInfo> fieldInfoList = new ArrayList<>();

    public String sql;

    public String toSave() {
        Gson gson = AuLiteSql.getGsonOrNew();
        return gson.toJson(this);
    }

    public static TableAssetInfo fromSave(@NonNull String save) {
        Gson gson = AuLiteSql.getGsonOrNew();
        return gson.fromJson(save, TableAssetInfo.class);
    }
}