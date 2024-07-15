package com.au.jobstudy.home;

/**
 * @author au
 * @date :2023/12/1 16:46
 * @description:
 */
public class HomeRcvTitleBean extends HomeRcvBean {
    public HomeRcvTitleBean(String title) {
        super(HomeRcvBean.VIEW_TYPE_TITLE);
        this.title = title;
    }

    public final String title;
    public boolean isFirstTitle;
}
