package com.allan.androidlearning.unused.base;


import com.allan.androidlearning.unused.beans.ids.ResId;

public abstract class IAllCode {
    /**
     * 读取所有代码 .CodeFile. 包括code+xml等。
     * 最好做好分类。方便后续查找。
     */
    public abstract void readAllCode();

    public abstract int containsDrawableMipmap(String mipmapName);

    public abstract int containsStyle(ResId id);
    public abstract int containsArray(ResId id);
    public abstract int containsString(ResId id);
    public abstract int containsColor(ResId id);
    public abstract int containsDimen(ResId id);
    public abstract int containsLayout(String layoutName, String bindingName);

    //public abstract int containsId(String idName);
    //public abstract int containsMenu(String menuName);
}
