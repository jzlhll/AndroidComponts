package com.au.aulitesql.info;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public final class FieldInfo implements Serializable {
    public String fieldName;
    //int boolean, long ,...
    public int dataType;

    public String name;

    //不要了，而是写在sqlStr里面
    // public T defaultValue;

    public String sql;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof FieldInfo other) {
            return Objects.equals(fieldName, other.fieldName)
                    && dataType == other.dataType
                    && Objects.equals(name, other.name)
                    && Objects.equals(sql, other.sql);
        }
        return false;
    }
}
