package com.au.jobstudy.home;

public class HomeRcvHeadBean extends HomeRcvBean {
    public HomeRcvHeadBean(String userName, String scroll) {
        super(HomeRcvBean.VIEW_TYPE_HEAD);
        this.scroll = scroll;
        this.userName = userName;
    }

    public final String userName;
    public final String scroll;
}
