package com.au.aulitesql.info;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;

@Keep
public final class FieldInfo {
    public final boolean beIgnore;
    @NonNull
    public final Field field;

    @NonNull
    public final String fieldName;
    //int boolean, long ,...
    public final int dataType;
    @NonNull
    public final String name;

    public final Object defaultValue;

    //不要了，而是写在sqlStr里面
    // public T defaultValue;
    @NonNull
    public final String sql;

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

    public FieldInfo(@NonNull Field field, @NonNull String fieldName, int dataType,
                     @NonNull String sql, boolean beIgnore,
                     @NonNull String name, Object defaultValue) {
        this.field = field;
        this.fieldName = fieldName;
        this.dataType = dataType;
        this.name = name;
        this.defaultValue = defaultValue;
        this.sql = sql;
        this.beIgnore = beIgnore;
    }
}
