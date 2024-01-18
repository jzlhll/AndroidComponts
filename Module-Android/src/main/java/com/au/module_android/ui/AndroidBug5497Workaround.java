package com.au.module_android.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * 解决WebView中输入时弹出软键盘，挡住输入框的问题
 */
public class AndroidBug5497Workaround {
    // For more information, see https://issuetracker.google.com/issues/36911528
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
    public static void assistActivity(@NonNull Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private final View mChildOfContent;
    private final FrameLayout.LayoutParams frameLayoutParams;
    private int usableHeightPrevious;
    private int frameLayoutHeight = 0;

    private AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                // keyboard probably just became visible
                frameLayoutHeight = frameLayoutParams.height;// ！！！修改前保存原有高度
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden
                if(0 != frameLayoutHeight) {
                    frameLayoutParams.height = frameLayoutHeight;// ！！！收起键盘恢复原有高度
                }
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }
}
