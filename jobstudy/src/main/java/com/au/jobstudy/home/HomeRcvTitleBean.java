package com.au.jobstudy.home;

public class HomeRcvTitleBean extends HomeRcvBean {
    public HomeRcvTitleBean(String title, boolean isWeekly) {
        super(HomeRcvBean.VIEW_TYPE_TITLE);
        this.title = title;
        this.isWeekly = isWeekly;
    }

    public final boolean isWeekly;
    public final String title;
    public boolean isFirstTitle;
}
