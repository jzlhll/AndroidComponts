package com.github.lzyzsd.jsbridge;


import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * @author au
 * @date :2024/1/16 10:05
 * @description:
 */
public class SystemUis {
    public static void myHideSystemUI(@NonNull android.app.Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(window, window.getDecorView());
        c.hide(WindowInsetsCompat.Type.systemBars());
        c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void myShowSystemUI(@NonNull android.app.Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowCompat.getInsetsController(window, window.getDecorView()).show(WindowInsetsCompat.Type.systemBars());
    }
}
