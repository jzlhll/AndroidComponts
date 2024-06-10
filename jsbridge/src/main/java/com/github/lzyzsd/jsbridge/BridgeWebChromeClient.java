package com.github.lzyzsd.jsbridge;

import static com.github.lzyzsd.jsbridge.SystemUis.myHideSystemUI;
import static com.github.lzyzsd.jsbridge.SystemUis.myShowSystemUI;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BridgeWebChromeClient extends WebChromeClient {
    public BridgeWebChromeClient(@NonNull BridgeExWebView webView) {
        super();
        this.webView = webView;
    }

    ////////////////////////////////
    ////// 实现H5网页选择图片支持
    ////////////////////////////////

    public interface IValueCallback {
        void onValueCallback(@Nullable ValueCallback<Uri[]> callback);
    }

    @Nullable
    public final IValueCallback getSelectPictureAction() {
        return this.selectPictureAction;
    }

    public final void setSelectPictureAction(@Nullable IValueCallback cb) {
        this.selectPictureAction = cb;
    }

    @Nullable
    private IValueCallback selectPictureAction;

    public boolean onShowFileChooser(@Nullable WebView webView,
                                     @Nullable ValueCallback<Uri[]> filePathCallback,
                                     @Nullable WebChromeClient.FileChooserParams fileChooserParams) {
        IValueCallback c = this.selectPictureAction;
        if (c == null) {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        } else {
            c.onValueCallback(filePathCallback);
            return true;
        }
    }

    ////////////////////////////////
    ////// 实现全屏播放
    ////////////////////////////////

    @Nullable
    private View mCustomView;

    @NonNull
    private final BridgeExWebView webView;

    @Nullable
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    public void onShowCustomView(@Nullable View view, @Nullable WebChromeClient.CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        ViewGroup fullLayout = webView.fullLayout;
        if (fullLayout == null) {
            return;
        }
        if (mCustomView != null) {
            if(callback != null) callback.onCustomViewHidden();
            return;
        }

        mCustomView = view; // 缓存全屏视图
        fullLayout.addView(mCustomView); // 向全屏控件添加全屏视图
        mCustomViewCallback = callback;
        webView.setVisibility(View.GONE); // 将已有webview控件隐藏
        if (webView.activity != null) {
            webView.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 切换横屏
            myHideSystemUI(webView.activity);
        }
    }

    public void onHideCustomView() {
        ViewGroup fullLayout = this.webView.fullLayout;
        View customView = this.mCustomView;
        if (fullLayout == null || customView == null) {
            super.onHideCustomView();
            return;
        }

        this.webView.setVisibility(View.VISIBLE);
        //当退出全屏时会执行当前方法
        //然后将frameLayout隐藏显示WebView就可以了，记得切换竖屏
        customView.setVisibility(View.GONE);
        fullLayout.removeView(customView);
        WebChromeClient.CustomViewCallback cb = this.mCustomViewCallback;
        if (cb != null) {
            cb.onCustomViewHidden();
        }
        mCustomView = null;
        //切换竖屏
        if (webView.activity != null) {
            myShowSystemUI(webView.activity);
            webView.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //later
        }

        super.onHideCustomView();
    }

    ////////////////////////////////
    ////// 去掉黑色播放按钮
    ////////////////////////////////
    @NonNull
    public Bitmap getDefaultVideoPoster() {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

//    @Override
//    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // 处理 console.log() 输出。android默认有日志。
//        Log.d(BridgeUtil.TAG, consoleMessage.message() + " -- From line "
//                + consoleMessage.lineNumber() + " of "
//                + consoleMessage.sourceId());
//        return super.onConsoleMessage(consoleMessage);
//    }
}

