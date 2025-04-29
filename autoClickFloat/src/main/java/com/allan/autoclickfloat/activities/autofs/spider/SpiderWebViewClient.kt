package com.allan.autoclickfloat.activities.autofs.spider

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import java.io.ByteArrayInputStream

class SpiderWebViewClient(val webView: SpiderWebView) : BridgeWebViewClient(webView) {
//    override fun onLoadResource(view: WebView?, url: String?) {
//        super.onLoadResource(view, url)
//        log("onLoad Resource $url")
//    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        log("onPage Finished $url")
        //todo 现在直接只解析第一个onPageFinish。也认为它一定有这个回调。其实可能会没有回调。
        val checkHtml = webView.checkHtml
        if (checkHtml != null) {
            webView.evaluateJavascript(
                "(function() { return document.documentElement.outerHTML; })();"
            ) { html ->
                checkHtml.checkHtml(html)
            }
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        log("onPage Started $url")
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString()
        val filter = webView.filter
        if (filter != null && !filter.isUrlSupport(url)) {
            //log(">>>>>>>>>>>>>>>>>>>>>InterceptRequest Yes $url")
            // 拦截并返回空响应（阻止加载）
            return WebResourceResponse(
                "text/plain", // MIME类型（根据实际需求调整）
                "UTF-8",      // 编码格式
                ByteArrayInputStream(ByteArray(0)) // 空字节流
            )
        }
        log("should InterceptRequest $url")
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        log("should OverrideUrlLoading ${request?.url}")
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        log("should OverrideUrlLoading $url")
        return super.shouldOverrideUrlLoading(view, url)
    }
}