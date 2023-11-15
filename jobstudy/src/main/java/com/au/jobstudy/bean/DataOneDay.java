package com.au.jobstudy.bean;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.annotation.AuLinkTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
public final class DataOneDay {
    @AuLinkTable(linkTable = DataItem.class, filterKey = "day")
    public final List<DataItem> items = new ArrayList<>(4);

    public String day;

    public void reload() {
        items.clear();
        items.addAll(AuLiteSql.getAllFilter(DataItem.class, "day", day, null, null, null));
    }
}