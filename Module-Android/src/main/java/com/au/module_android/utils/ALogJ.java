package com.au.module_android.utils;

import android.os.Looper;

import java.util.Locale;

public class ALogJ {
    public static String log(String lvl, String s, Class<?> javaClass) {
        var log = javaClass.toString();
        return lvl + " " + log.substring(log.lastIndexOf('.') + 1) + ": " + s;
    }

    public static String logt(String s) {
        var id = Thread.currentThread().getId();
        if (id == Looper.getMainLooper().getThread().getId()) {
            return "MainThread: " + s;
        } else {
            return String.format(Locale.ROOT, "SubThread[%02d]: %s", id, s);
        }
    }

    public static String log(String lvl, String s, String tag, Class<?> javaClass) {
        var log = javaClass.toString();
        return lvl + " " + log.substring(log.lastIndexOf('.') + 1) + ": " + tag + ": " + s;
    }

    public static String ex(Throwable e) {
        StringBuilder sb = new StringBuilder();
        var msg = e.getMessage();
        if(msg != null && !msg.isEmpty()) sb.append(msg).append(System.lineSeparator());
        var cause = e.getCause();
        if(cause != null) sb.append(cause).append(System.lineSeparator());

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append(System.lineSeparator());
        }

        return sb.toString();
    }
}