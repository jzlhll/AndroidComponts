package com.au.jobstudy.bean;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.Entity;
import com.au.aulitesql.actions.GsonUtil;

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

    public int minPicNum;
    public int maxPicNum;

    public int minVoiceSec;
    public int maxVoiceSec;

    public int checkUpMode;

    public String day; //20230910

    public String weekStartDay; //20231127就是一个星期一

    @Override
    public void reset() {
        orderIndex = 0;
        subject = "";
        desc = "";
        complete = false;
        completeTime = 0;
        if(records != null) records.clear();
        minVideoSec = maxVideoSec = 0;
        minPicNum = maxPicNum = 0;
        minVoiceSec = maxVoiceSec = 0;
        checkUpMode = 0;
        day = null;
        weekStartDay = null;
    }

    @Override
    public void pack(@NonNull ContentValues cv) {
        cv.put("orderIndex", orderIndex);
        cv.put("subject", subject);
        cv.put("desc", desc);
        cv.put("complete", complete);
        cv.put("completeTime", completeTime);
        String recordsStr = gson().toJson(records);
        cv.put("records", recordsStr);
        cv.put("minVideoSec", minVideoSec);
        cv.put("maxVideoSec", maxVideoSec);
        cv.put("minPicNum", minPicNum);
        cv.put("maxPicNum", maxPicNum);
        cv.put("minVoiceSec", minVoiceSec);
        cv.put("maxVoiceSec", maxVoiceSec);
        cv.put("checkUpMode", checkUpMode);
        cv.put("day", day);
        cv.put("weekStartDay", weekStartDay);
    }

    @Override
    public void unpack(@NonNull Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("orderIndex");
        if (columnIndex >= 0) orderIndex = cursor.getInt(columnIndex);

        columnIndex = cursor.getColumnIndex("subject");
        if (columnIndex >= 0) subject = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex("desc");
        if (columnIndex >= 0) desc = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex("complete");
        if (columnIndex >= 0) complete = cursor.getInt(columnIndex) != 0;

        columnIndex = cursor.getColumnIndex("index");
        if (columnIndex >= 0) completeTime = cursor.getLong(columnIndex);

        columnIndex = cursor.getColumnIndex("records");
        if (columnIndex >= 0) records = GsonUtil.gsonFromList(cursor.getString(columnIndex), DataItem.MediaItem.class);

        columnIndex = cursor.getColumnIndex("minVideoSec");
        if (columnIndex >= 0) minVideoSec = cursor.getInt(minVideoSec);

        columnIndex = cursor.getColumnIndex("maxVideoSec");
        if (columnIndex >= 0) maxVideoSec = cursor.getInt(maxVideoSec);

        columnIndex = cursor.getColumnIndex("index");
        if (columnIndex >= 0) minPicNum = cursor.getInt(minPicNum);

        columnIndex = cursor.getColumnIndex("maxPicNum");
        if (columnIndex >= 0) maxPicNum = cursor.getInt(columnIndex);

        columnIndex = cursor.getColumnIndex("minVoiceSec");
        if (columnIndex >= 0) minVoiceSec = cursor.getInt(columnIndex);

        columnIndex = cursor.getColumnIndex("maxVoiceSec");
        if (columnIndex >= 0) maxVoiceSec = cursor.getInt(columnIndex);

        columnIndex = cursor.getColumnIndex("day");
        if (columnIndex >= 0) day = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex("day");
        if (columnIndex >= 0) day = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex("weekStartDay");
        if (columnIndex >= 0) weekStartDay = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex("checkUpMode");
        if (columnIndex >= 0) checkUpMode = cursor.getInt(columnIndex);
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