package com.au.aulitesql.info;

import com.au.aulitesql.Entity;

/**
 * @author au
 * @date :2023/11/13 15:24
 * @description:
 */
public class NamesPair {
    public NamesPair(String oldName, Class<? extends Entity> newNameClass) {
        this.oldName = oldName;
        this.newNameClass = newNameClass;
    }

    public final String oldName;
    public final Class<? extends Entity> newNameClass;
}
