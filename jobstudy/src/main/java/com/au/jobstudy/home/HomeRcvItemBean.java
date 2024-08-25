package com.au.jobstudy.home;

import androidx.annotation.NonNull;

import com.au.jobstudy.check.bean.WorkEntity;

public class HomeRcvItemBean extends HomeRcvBean {
    public HomeRcvItemBean(@NonNull WorkEntity oneWork) {
        super(HomeRcvBean.VIEW_TYPE_ITEM);
        this.oneWork = oneWork;
    }

    @NonNull
    public final WorkEntity oneWork;
}
