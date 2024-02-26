package com.github.lzyzsd.jsbridge;

import android.os.Looper;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author allan
 * @date :2024/2/26 17:34
 * @description:
 */
public class BridgeManager {
    @NonNull
    private final BridgeWebView webView;
    BridgeManager(@NonNull BridgeWebView webView) {
        this.webView = webView;
    }

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();

    BridgeHandler findHandler(@Nullable String handleName) {
        if (handleName != null && messageHandlers.containsKey(handleName)) {
            BridgeHandler handler = messageHandlers.get(handleName);
            if (handler != null) {
                return handler;
            }
        }
        return defaultHandler;
    }

    private BridgeHandler defaultHandler = new DefaultHandler();
    /**
     *
     * @param handler
     *            default handler,handle messages send by js without assigned handler name,
     *            if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    /**
     * register handler,so that javascript can call it
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    private List<Message> startupMessage = new ArrayList<Message>();

    void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    /////////////////////////////////////////////////////
    //想要得到回调，传统写法，通过将callback暂存。最后js再次执行jsCallResponse。再次取出执行。
    /////////////////////////////////////////////////////
    private long uniqueId = 0;
    final Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
    void webViewCallJsWithResponse(String handleName, String json, @NonNull CallBackFunction cb) {
        //我们都通过回调的方式来兼容以前的jsbridge写法。
        String id = "javaCallJsId_" + uniqueId++;

        Message message = new Message();
        message.setData(json);
        message.setCallbackId(id);
        message.setHandlerName(handleName);

        responseCallbacks.put(id, cb);

        queueMessage(message);
    }

    /**
     *  不兼容我们现在的框架，而且使用的时候还要注意必须已经初始化好了才能使用。
     */
    @Deprecated
    void webViewCallJsWithResponseNew(String handleName, String json, @NonNull CallBackFunction cb) {
        if (startupMessage != null) { //还没有初始化好jsbridge就调用了。则使用传统方式来暂存处理。
            webViewCallJsWithResponse(handleName, json, cb);
        } else {
            //新写法可以要求js，不用做这种二层function而直接回调。但是要求js 将_handleMessageFromNativeNew 立刻返回执行结果。
            Message message = new Message();
            message.setData(json);
            message.setHandlerName(handleName);
            webView.evaluateJavascript(String.format("javascript:WebViewJavascriptBridge._handleMessageFromNativeNew('%s');", message.toJson()), cb::onCallBack);
        }
    }

    //直接调用，不需要返回参数。因此，id不传入。
    void webViewCallJs(String handleName, String json) {
        Message message = new Message();
        message.setData(json);
        message.setHandlerName(handleName);

        queueMessage(message);
    }

    public void webViewResponseToJs(String jsCallId, String responseJsonFromJava) {
        Message message = new Message();
        message.setData(responseJsonFromJava);
        message.setCallbackId(jsCallId);

        queueMessage(message);
    }

    private void queueMessage(Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    private void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = JSONObject.quote(messageJson);
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            webView.evaluateJavascript(javascriptCommand, null);
        }
    }

}
