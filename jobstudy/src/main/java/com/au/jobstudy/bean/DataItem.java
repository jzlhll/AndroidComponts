package com.au.jobstudy.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
public final class DataItem implements Serializable {
    public int index;

    @NonNull
    public String subject = "";

    @NonNull
    public String desc = "";

    public boolean complete;

    public long completeTime;

    /**
     * 打卡记录的文件路径
     */
    @Nullable
    public List<MediaItem> records;

    public int minVideoSec;
    public int maxVideoSec;

    public int minPicNum;
    public int maxPicNum;

    public int minVoiceSec;
    public int maxVoiceSec;

    public static final class MediaItem {
        public static final int TYPE_VIDEO = 1;
        public static final int TYPE_VOICE = 2;
        public static final int TYPE_PIC = 3;

        public int type;

        public String path;
    }
}