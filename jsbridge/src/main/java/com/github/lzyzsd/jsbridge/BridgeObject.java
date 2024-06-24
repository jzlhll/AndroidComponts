package com.github.lzyzsd.jsbridge;

import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BridgeObject {
    @NonNull
    private final BridgeWebView webView;

    BridgeObject(@NonNull BridgeWebView v) {
        webView = v;
    }

    //用于js片段中。直接调用java. java不得调用。
    @JavascriptInterface
    public void jsCall(String json) {
        Message msg = Message.toObject(json);
        String handleName = msg.getHandlerName();
        BridgeUtil.getMainHandler().post(()->{
            if (!webView.isAttachedToWindow()) return;
            webView.findHandler(handleName).handler(msg.getData(), data -> webViewResponseToJs(msg.getId(), data));
        });
    }

    //用于js片段中。用于java调用js以后，js再往回调用。而第一个参数，是java往js调用时传入的。
    @JavascriptInterface
    public void jsResponse(String json) {
        Message msg = Message.toObject(json);
        String originJavaCallId = msg.getId();
        BridgeUtil.getMainHandler().post(()->{
            if (!webView.isAttachedToWindow()) return;
            if (responseCallbacks.containsKey(originJavaCallId)) {
                CallBackFunction f = responseCallbacks.get(originJavaCallId);
                if (f != null) {
                    f.onCallBack(msg.getData());
                    responseCallbacks.remove(originJavaCallId);
                } else {
                    Log.w(BridgeUtil.TAG, "callResponseFromJs is null json: " + json);
                }
            }
        });
    }

    /////////////////////////////////////////

    private List<Message> startupMessage = new ArrayList<>();

    void clearStartupMessage() {
        if (startupMessage != null) {
            for (Message m : startupMessage) {
                dispatchMessage(m);
            }
        }
        this.startupMessage = null;
    }

    /////////////////////////////////////////////////////
    //想要得到回调，传统写法，通过将callback暂存。最后js再次执行jsCallResponse。再次取出执行。
    /////////////////////////////////////////////////////
    private long uniqueId = 0;
    private final Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
    void webViewCallJsWithResponse(String handleName, String json, @NonNull CallBackFunction cb) {
        //我们都通过回调的方式来兼容以前的jsbridge写法。
        String id = "javaCallJsId_" + uniqueId++ + "_" + System.currentTimeMillis();

        Message message = new Message();
        message.setData(json);
        message.setId(id);
        message.setHandlerName(handleName);

        responseCallbacks.put(id, cb);

        queueMessage(message);
    }

    //直接调用，不需要返回参数。因此，id不传入。
    void webViewCallJs(String handleName, String json) {
        Message message = new Message();
        message.setData(json);
        message.setHandlerName(handleName);

        queueMessage(message);
    }

    //当js调用BridgeObject jsCall后，我们将会通过该函数返回结果。需要配合嵌入js代码实现。因此，将js的id传回去。
    private void webViewResponseToJs(String jsCallId, String responseJsonFromJava) {
        Message message = new Message();
        message.setData(responseJsonFromJava);
        message.setId(jsCallId);
        message.setResponseCodeAsJavaResponseToJs();

        queueMessage(message);
    }

    private void queueMessage(@NonNull Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

//    private static final String percent7B = URLEncoder.encode("%7B");
//    private static final String percent7D = URLEncoder.encode("%7D");
//    private static final String percent22 = URLEncoder.encode("%22");

    private void dispatchMessage(@NonNull Message m) {
        String messageJson = m.toJson();
        if (messageJson == null) {return;}
        //escape special characters for json string  为json字符串转义特殊字符
        //1. 过时
//        messageJson = messageJson
//                .replaceAll("(?<=[^\\\\])(')", "\\\\'")
//                .replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2")
//                .replaceAll("(?<=[^\\\\])(\")", "\\\\\"")
//                .replaceAll("%7B", percent7B)
//                .replaceAll("%7D", percent7D)
//                .replaceAll("%22", percent22);

        //2. 新的处理方式 效率更高。但是会存在多余的外部2个引号。
        messageJson = JSONObject.quote(messageJson);
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson.substring(1, messageJson.length() - 1));
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            webView.evaluateJavascript(javascriptCommand, null);
        } else {
            BridgeUtil.getMainHandler().post(()-> {
                if (webView.isAttachedToWindow()) {
                    webView.evaluateJavascript(javascriptCommand, null);
                }
            });
        }
    }

//    /**
//     * @Deprecated 不兼容我们现在的框架，而且使用的时候还要注意必须已经初始化好了才能使用。
//     */
//    void webViewCallJsWithResponseNew(String handleName, String json, @NonNull CallBackFunction cb) {
//        if (startupMessage != null) { //还没有初始化好jsbridge就调用了。则使用传统方式来暂存处理。
//            webViewCallJsWithResponse(handleName, json, cb);
//        } else {
//            //新写法可以要求js，不用做这种二层function而直接回调。但是要求js 将_handleMessageFromNativeNew 立刻返回执行结果。
//            Message message = new Message();
//            message.setData(json);
//            message.setHandlerName(handleName);
//
//            webView.evaluateJavascript(String.format("javascript:WebViewJavascriptBridge._handleMessageFromNativeNew('%s');", message.toJson()), cb::onCallBack);
//        }
//    }
}
