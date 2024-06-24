package com.github.lzyzsd.jsbridge;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BridgeExWebView extends BridgeWebView {
    @Nullable
    public ViewGroup fullLayout;

    @Nullable
    public Activity activity;

    public BridgeExWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BridgeExWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BridgeExWebView(Context context) {
        super(context);
    }

    public interface OnH5EventListener {
        /**
         * 接收到的H5的事件分发
         */
        void onH5Event(
                @NonNull BridgeExWebView webView,
                @NonNull String eventName,
                @Nullable String msg,
                @NonNull CallBackFunction call
        );
    }

    ///////////////////////
    /// 用于这个WebView显示在底部弹窗dialog上，避免触发事件到了下面，导致滑动bug。
    //////////////////////
    /**
     * 传入true能让他停止父类的事件。
     */
    private boolean shouldInterruptParent = false;
    public void disableParentInterrupt() {
        shouldInterruptParent = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (shouldInterruptParent) {
            ViewParent p = getParent();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN ->
                    p.requestDisallowInterceptTouchEvent(true);
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL->
                    p.requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    ///////////////////////
    /// 添加chromeClient。用于处理H5选择图片
    //////////////////////
    @Nullable
    private BridgeWebChromeClient mChromeClient;

    @Override
    protected BridgeWebViewClient generateBridgeWebViewClient() {
        mChromeClient = new BridgeWebChromeClient(this);
        setWebChromeClient(mChromeClient);//在设置chromeClient的同时，设置，
        return super.generateBridgeWebViewClient();
    }

    public void setSelectPictureAction(BridgeWebChromeClient.IValueCallback callback) {
        if (mChromeClient != null) {
            mChromeClient.setSelectPictureAction(callback);
        }
    }

    private final List<OnH5EventListener> events = new ArrayList<>();

    /**
     * 添加消息监听
     */
    public void addOnH5EventListener(@NonNull OnH5EventListener listener) {
        if (!events.contains(listener)) {
            events.add(listener);
        }
    }

    /**
     * 移除消息监听
     */
    public void removeOnH5EventListener(@Nullable OnH5EventListener listener) {
        if(listener != null) events.remove(listener);
    }

    /**
     * 注册监听h5事件
     *
     */
    public void registerH5Event(@NonNull String eventName) {
        registerHandler(eventName, (data, function) -> {
            events.forEach(it -> {
                it.onH5Event(this, eventName, data, function);
            });
        });
    }

    private CallBackFunction sendEventToH5CbFunction;

    @NonNull
    private CallBackFunction requireSendEventToH5bFunction() {
        if (sendEventToH5CbFunction == null) {
            sendEventToH5CbFunction = data -> {
            };
        }
        return sendEventToH5CbFunction;
    }

    /**
     * 发送信息给h5
     */
    public void sendEventToH5(@NonNull String eventName, @Nullable String msg) {
        //必须有一个handler，否则会在console不断打印错误。
        callHandler(eventName, msg, requireSendEventToH5bFunction());
    }

    /**
     * 发送信息给h5。追加一个带callback的函数。
     */
    public void sendEventToH5WithCallback(@NonNull String eventName, @Nullable String msg, @NonNull CallBackFunction callback) {
        callHandler(eventName, msg, callback);
    }
}
