package com.allan.mydroid.views.textchat.uibean;

public abstract class AbsItem {
    public final int viewType;

    public AbsItem(int viewType) {
        this.viewType = viewType;
    }

    public static final int VIEW_TYPE_STATUS = 0;
    /**
     * 是不是我的消息
     */
    public static final int VIEW_TYPE_ME = 1;
    /**
     * 是否是别人的消息
     */
    public static final int VIEW_TYPE_OTHER = 2;
}
