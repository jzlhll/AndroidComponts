package com.au.aulitesql;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * @author allan.jiang
 * @date :2023/11/15 17:14
 */
public final class ListDao {
    public <E extends EntityTable> List<E> load(Class<E> clazz) {
        try {
            return AuLiteSql.loadAllData(clazz);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public <E extends EntityTable, P> List<E> loadFilter(Class<E> clazz, String key, P value) {
        return AuLiteSql.loadAllData(clazz, key, value, null, null, null);
    }
}