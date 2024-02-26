package com.github.lzyzsd.jsbridge;

import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

public class BridgeObject {
    @NonNull
    private final BridgeManager mMgr;

    BridgeObject(@NonNull BridgeManager mgr) {
        mMgr = mgr;
    }

    //用于js直接调用java. java不得调用。
    @JavascriptInterface
    public void jsCall(String jsCallId, String handleName, String jsonFromJs) {
        mMgr.findHandler(handleName).handler(jsonFromJs, data -> {
            mMgr.webViewResponseToJs(jsCallId, data);
        });
    }

    //仅仅用于java调用js以后，js再往回调用。而第一个参数，是java往js调用时传入的。
    @JavascriptInterface
    public void jsCallResponse(String originJavaCallId, String responseJsonFromJs) {
        if (mMgr.responseCallbacks.containsKey(originJavaCallId)) {
            CallBackFunction f = mMgr.responseCallbacks.get(originJavaCallId);
            if (f != null) {
                f.onCallBack(responseJsonFromJs);
            } else {
                Log.w(BridgeUtil.TAG, "callResponseFromJs is null json: " + responseJsonFromJs);
            }
        }
    }

    /////////////////////////////////////////
}
