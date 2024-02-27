package com.github.lzyzsd.jsbridge;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private final BridgeWebView webView;

    private boolean isLoadedBridgeJs = false;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (!isLoadedBridgeJs) {
            BridgeUtil.getMainHandler().postDelayed(() -> {
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

    private void loadJs(WebView view) {
        Log.d(BridgeUtil.TAG, "load js!!");
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        Log.d(BridgeUtil.TAG, "load js end!!");
        onLoadJsExtra();
        webView.bridgeObject.clearStartupMessage();
    }

    /**
     * 允许当js完成后的回调。用于二次设定额外的js。
     */
    protected void onLoadJsExtra() {}
}