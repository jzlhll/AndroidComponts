package com.github.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    public final String TAG = BridgeUtil.TAG;

    public static final String toLoadJs = "WebViewJavascriptBridge.js";
    private final Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
    private final Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    BridgeHandler defaultHandler = new DefaultHandler();

    private List<Message> startupMessage = new ArrayList<Message>();

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    private long uniqueId = 0;

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BridgeWebView(Context context) {
        super(context);
        init();
    }

    /**
     *
     * @param handler
     *            default handler,handle messages send by js without assigned handler name,
     *            if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    private void init() {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
		this.setWebViewClient(generateBridgeWebViewClient());
	}

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return new BridgeWebViewClient(this);
    }

    void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            return;
        }
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
        String callbackStr;
        if (responseCallback != null) {
            callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
        } else {
            callbackStr = null;
        }

        Message[] messages = Message.createSend(callbackStr, data, handlerName);
        queueMessage(messages);
    }

    private void queueMessage(@NonNull Message[] messages) {
        for (Message m : messages) {
            if (startupMessage != null) {
                startupMessage.add(m);
            } else {
                dispatchMessage(m);
            }
        }
    }

    void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    private TimeSlowHandler timeSlowHandler;
    private TimeSlowHandler getTimeSlowHandler() {
        if (timeSlowHandler == null) {
            timeSlowHandler = new TimeSlowHandler(getHandler());
        }
        return timeSlowHandler;
    }

    void flushMessageQueue() {
        getTimeSlowHandler().execute(flushMessageQueueRun);
    }

    private final Runnable flushMessageQueueRun = () -> {
        if (Thread.currentThread() == Looper.getMainLooper().getThread() && isAttachedToWindow()) {
            loadUrlWithResponse(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, data -> {
                // deserializeMessage
                List<Message> list = Message.toArrayList(data);
                for (Message m : list) {
                    String responseId = m.getResponseId();
                    // 是否是response
                    if (!TextUtils.isEmpty(responseId)) {
                        CallBackFunction function = responseCallbacks.get(responseId);
                        String responseData = m.getResponseData();
                        if(function != null) function.onCallBack(responseData);
                        responseCallbacks.remove(responseId);
                    } else {
                        CallBackFunction responseFunction;
                        // if had callbackId
                        final String callbackId = m.getCallbackId();
                        if (!TextUtils.isEmpty(callbackId)) {
                            responseFunction = dat -> {
                                queueMessage(Message.createResponse(dat, callbackId));
                            };
                        } else {
                            responseFunction = dat -> {
                                // do nothing
                                Log.d(TAG, "do nothing in responseFunction");
                            };
                        }
                        BridgeHandler handler;
                        if (!TextUtils.isEmpty(m.getHandlerName())) {
                            handler = messageHandlers.get(m.getHandlerName());
                        } else {
                            handler = defaultHandler;
                        }
                        if (handler != null) {
                            handler.handler(m.getData(), responseFunction);
                        }
                    }
                }
            });
        }
    };

    private void loadUrlWithResponse(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
        responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName
     * @param handler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName
     * @param data
     * @param callBack
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }
}
