package com.au.audiorecordplayer.cam2.base;

public class FeatureUtil {

    public static final int FEATURE_NONE = -1;

    /**
     * 表示拍照的能力
     */
    public static final int FEATURE_PICTURE = 0x010;
    /**
     * 表示预览的能力
     */
    public static final int FEATURE_PREVIEW = 0x001;
    /**
     * 表示录制的能力
     */
    public static final int FEATURE_RECORD_VIDEO = 0x100;

    public static final String MODE_PREVIEW = "preview";
    public static String MODE_PREVIEW_PICTURE = "preview_and_take_picture";
    public static String MODE_PREVIEW_PICTURE_VIDEO = "preview_and_take_picture_and_video";
}
