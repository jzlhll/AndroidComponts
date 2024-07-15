package com.au.jobstudy.home;

/**
 * @author au
 * @date :2023/12/1 16:46
 * @description:
 */
public class HomeRcvBean {
    public final int viewType;
    public HomeRcvBean(int viewType) {
        this.viewType = viewType;
    }

    public static final int VIEW_TYPE_MARKUP = 0;
    public static final int VIEW_TYPE_HEAD = 1;
    public static final int VIEW_TYPE_TITLE = 2;
    public static final int VIEW_TYPE_ITEM = 3;

    public static final HomeRcvBean empty = new HomeRcvBean(VIEW_TYPE_MARKUP);
}
