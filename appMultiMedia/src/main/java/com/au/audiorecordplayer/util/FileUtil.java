package com.au.audiorecordplayer.util;

import java.io.File;

public class FileUtil {
    public static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
