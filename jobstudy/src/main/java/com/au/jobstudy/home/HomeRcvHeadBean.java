package com.au.jobstudy.home;

public class HomeRcvHeadBean extends HomeRcvBean {
    public HomeRcvHeadBean(int viewType, String userName, String scroll) {
        super(viewType);
        this.scroll = scroll;
        this.userName = userName;
    }

    public final String userName;
    public final String scroll;
    public int starCount;
    public int dingCount;
}
