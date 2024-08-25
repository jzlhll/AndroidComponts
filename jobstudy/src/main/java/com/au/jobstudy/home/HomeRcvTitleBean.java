package com.au.jobstudy.home;

public class HomeRcvTitleBean extends HomeRcvBean {
    public HomeRcvTitleBean(String title, int colorIndex) {
        super(HomeRcvBean.VIEW_TYPE_TITLE);
        this.title = title;
        this.colorIndex = colorIndex;
    }

    public final int colorIndex;
    public final String title;
    public boolean isFirstTitle;
}
