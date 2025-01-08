package com.au.module_android.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * 解决WebView中输入时弹出软键盘，挡住输入框的问题
 */
public class AndroidBug5497Workaround {
    private final View mChildOfContent;
    private final FrameLayout.LayoutParams frameLayoutParams;
    private int lastUseableHeight;
    private final int origFrameLayoutHeight;

    private final ViewTreeObserver.OnGlobalLayoutListener listener;

    public AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        origFrameLayoutHeight = frameLayoutParams.height;
        if (origFrameLayoutHeight == ViewGroup.LayoutParams.MATCH_PARENT || origFrameLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            listener = this::possiblyResizeChildOfContent;
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        } else {
            listener = null;
        }
    }

    public void onDestroy() {
        if(listener != null) mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    private void possiblyResizeChildOfContent() {
        int usableHeight = computeUsableHeight();

        if (usableHeight != lastUseableHeight) {
            int totalHeight = mChildOfContent.getRootView().getHeight();
            int heightDifference = totalHeight - usableHeight;
            if (heightDifference > (totalHeight/4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = totalHeight - heightDifference;
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = origFrameLayoutHeight;// ！！！收起键盘恢复原有高度
            }
            mChildOfContent.requestLayout();
            lastUseableHeight = usableHeight;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }
}