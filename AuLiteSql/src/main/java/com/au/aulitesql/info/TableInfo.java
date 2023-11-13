package com.au.aulitesql.info;

import com.au.aulitesql.EntityTable;

import java.util.ArrayList;
import java.util.List;

public final class TableInfo {
    public Class<? extends EntityTable> entityTable;

    public String className;

    public String name;

    public final List<FieldInfo> fieldInfoList = new ArrayList<>();

    public String sql;
}