package com.au.jobstudy.home;

import androidx.annotation.NonNull;

import com.au.jobstudy.check.bean.WorkEntity;

public class HomeRcvItemBean extends HomeRcvBean {
    public HomeRcvItemBean(int viewType, int colorId, @NonNull WorkEntity oneWork) {
        super(viewType);
        this.colorId = colorId;
        this.oneWork = oneWork;
    }

    public final int colorId;

    @NonNull
    public final WorkEntity oneWork;
}
