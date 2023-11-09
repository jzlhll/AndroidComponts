package com.au.jobstudy.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
public final class DataOneDay implements Serializable {
    int index;

    @NonNull
    String subject;

    @NonNull
    String desc;

    boolean complete;

    long completeTime;
}
