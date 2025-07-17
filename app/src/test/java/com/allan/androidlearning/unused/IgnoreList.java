package com.allan.androidlearning.unused;

public class IgnoreList {
    /**
     * 根据后缀是否可以读取为文本文件
     */
    public static boolean canFileReadText(String extension) {
        return !"webp".equalsIgnoreCase(extension)
                && !"png".equalsIgnoreCase(extension)
                && !"jpg".equalsIgnoreCase(extension)
                && !"jpeg".equalsIgnoreCase(extension)
                && !"gif".equalsIgnoreCase(extension)
                && !"mp3".equalsIgnoreCase(extension)
                && !"mp4".equalsIgnoreCase(extension)
                && !"json".equalsIgnoreCase(extension)
                && !"ttf".equalsIgnoreCase(extension)
                && !"otf".equalsIgnoreCase(extension)
                && !"zip".equalsIgnoreCase(extension);
    }
}
