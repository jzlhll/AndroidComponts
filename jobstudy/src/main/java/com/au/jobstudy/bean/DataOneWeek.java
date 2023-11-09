package com.au.jobstudy.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataOneWeek {
    public final Map<Integer, ArrayList<DataOneDay>> week = new HashMap<>(8, 1f);

    public long startTime;
    public long endTime;

    public String week1;
}
