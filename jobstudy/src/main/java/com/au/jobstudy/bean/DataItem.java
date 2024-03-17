package com.au.jobstudy.bean;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
@Keep
public final class DataItem extends Entity implements Serializable {
    public int orderIndex;

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

    public boolean isSupportVideo() {
        return minVideoSec > 0 && maxVideoSec >= minVideoSec;
    }

    public int minPicNum;
    public int maxPicNum;

    public boolean isSupportPicture() {
        return minPicNum > 0 && maxPicNum >= minPicNum;
    }

    public int minVoiceSec;
    public int maxVoiceSec;

    public boolean isSupportVoice() {
        return minVoiceSec > 0 && maxVoiceSec >= minVoiceSec;
    }

    public String day; //20230910

    public String weekStartDay; //20231127就是一个星期一

    @NonNull
    @Override
    public String toString() {
        return subject + " " + day + " " + weekStartDay + " " + desc;
    }

    @Keep
    public static final class MediaItem {
        public static final int TYPE_VIDEO = 0x1;
        public static final int TYPE_VOICE = 0x2;
        public static final int TYPE_PIC = 0x4;

        public int type;

        public String path;
    }
}