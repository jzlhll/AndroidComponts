package com.au.jobstudy.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
public final class DataOneDay {
    public final List<DataItem> items = new ArrayList<>(4);

    public long startTime;
    public long endTime;
}