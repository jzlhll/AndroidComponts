package com.au.module_android.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;

import com.au.module_android.Globals;

/**
 * 解决WebView中输入时弹出软键盘，挡住输入框的问题
 */
public class AndroidBug5497Workaround {
    private final View mChildOfContent;
    private final FrameLayout.LayoutParams frameLayoutParams;
    private int lastUseableHeight;
    private static final int ORIG_HEIGHT_FLAG = Integer.MIN_VALUE >> 1 + 2;
    private int origFrameLayoutHeight = ORIG_HEIGHT_FLAG;

    public AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

        Globals.INSTANCE.getMainHandler().postDelayed(() -> {
            origFrameLayoutHeight = frameLayoutParams.height;
        }, 100);
    }

    public void onDestroy() {
        mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
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
                if(origFrameLayoutHeight != ORIG_HEIGHT_FLAG) frameLayoutParams.height = origFrameLayoutHeight;// ！！！收起键盘恢复原有高度
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