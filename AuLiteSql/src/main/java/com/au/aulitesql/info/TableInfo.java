package com.au.aulitesql.info;


import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.EntityTable;
import com.google.gson.Gson;

public final class TableInfo extends TableAssetInfo {
    public transient Class<? extends EntityTable> entityTable;

    public static TableInfo fromSave(@NonNull String save) {
        Gson gson = AuLiteSql.getGsonOrNew();
        return gson.fromJson(save, TableInfo.class);
    }
}