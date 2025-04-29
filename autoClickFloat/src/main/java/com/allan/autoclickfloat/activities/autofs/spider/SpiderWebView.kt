package com.allan.autoclickfloat.activities.autofs.spider

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebViewClient
import com.allan.autoclickfloat.activities.autofs.spider.checkhtml.AbstractCheckHtml
import com.allan.autoclickfloat.activities.autofs.spider.filter.WebViewFilterBase
import com.github.lzyzsd.jsbridge.BridgeExWebView

class SpiderWebView : BridgeExWebView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    var filter:WebViewFilterBase? = null
    var checkHtml: AbstractCheckHtml? = null

    override fun generateBridgeWebViewClient(): WebViewClient {
        return SpiderWebViewClient(this)
    }
}