package com.au.jobstudy.check.modes;


import androidx.annotation.IntRange;

public final class MediaItem {
    @IntRange(from = MediaType.TYPE_VIDEO, to = MediaType.TYPE_PARENT)
    public int mediaType;
    public String path;
}