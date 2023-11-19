package com.au.jobstudy.bean;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.aulitesql.Entity;
import com.au.aulitesql.actions.GsonUtil;

import java.io.Serializable;
import java.util.List;

/**
 * 科目，任务描述，是否完成，完成的时间点
 */
public final class DataItem extends Entity implements Serializable {
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

    public String day; //20230910

    @Override
    public void reset() {
        index = 0;
        subject = "";
        desc = "";
        complete = false;
        completeTime = 0;
        if(records != null) records.clear();
        minVideoSec = maxVideoSec = 0;
        minPicNum = maxPicNum = 0;
        minVoiceSec = maxVoiceSec = 0;
        day = null;
    }

    @Override
    public void pack(@NonNull ContentValues cv) {
        cv.put("index", index);
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
        cv.put("day", day);
    }

    @Override
    public void unpack(@NonNull Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("index");
        if (columnIndex >= 0) index = cursor.getInt(columnIndex);

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
    }

    public static final class MediaItem {
        public static final int TYPE_VIDEO = 1;
        public static final int TYPE_VOICE = 2;
        public static final int TYPE_PIC = 3;

        public int type;

        public String path;
    }
}