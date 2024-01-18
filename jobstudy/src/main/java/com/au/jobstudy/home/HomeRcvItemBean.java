package com.au.jobstudy.home;

import androidx.annotation.NonNull;

import com.au.jobstudy.bean.DataItem;

/**
 * @author allan
 * @date :2023/12/1 16:46
 * @description:
 */
public class HomeRcvItemBean extends HomeRcvBean{
    public HomeRcvItemBean(int viewType, int colorId, @NonNull DataItem dataItem) {
        super(viewType);
        this.colorId = colorId;
        this.dataItem = dataItem;
    }

    public final int colorId;

    @NonNull
    public final DataItem dataItem;
}
