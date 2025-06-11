package com.allan.mydroid.views.textchat.uibean;

public abstract class AbsItem {
    public final int viewType;

    public AbsItem(int viewType) {
        this.viewType = viewType;
    }

    public static final int VIEW_TYPE_STATUS = 0;
    public static final int VIEW_TYPE_ME = 1;
    public static final int VIEW_TYPE_OTHER = 2;
}
