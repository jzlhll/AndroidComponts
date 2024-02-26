package com.github.lzyzsd.jsbridge;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private final BridgeWebView webView;

    private boolean isLoadedBridgeJs = false;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        try {
//            url = URLDecoder.decode(url, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
//            webView.handlerReturnData(url);
//            return true;
//        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
//            webView.flushMessageQueue();
//            return true;
//        } else {
//            return super.shouldOverrideUrlLoading(view, url);
//        }
//    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        var url = request.getUrl().toString();
        //Log.d(BridgeUtil.TAG, "shouldOverride UrlLoading: " + url);
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (!isLoadedBridgeJs) {
            webView.postDelayed(() -> {
                if (!isLoadedBridgeJs && webView.isAttachedToWindow()) {
                    isLoadedBridgeJs = true;
                    loadJs(webView);
                }
            }, 500);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (!isLoadedBridgeJs) {
            isLoadedBridgeJs = true;
            loadJs(view);
        }
    }

    public void loadJs(WebView view) {
        Log.d(BridgeUtil.TAG, "load js!!");
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        Log.d(BridgeUtil.TAG, "load js end!!");

        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }
}