package com.au.module_androidex.jsbridge

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.au.module_android.postToMainHandler
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.asOrNull
import com.au.module_androidex.R
import com.github.lzyzsd.jsbridge.BridgeExWebView
import com.github.lzyzsd.jsbridge.CallBackFunction

/**
 * @author au
 * @date :2024/1/19 14:50
 * @description:
 */
class BridgeWebViewExFragment : ViewFragment(), BridgeExWebView.OnH5EventListener {
    private lateinit var webView:BridgeExWebView
    private lateinit var fullVideoLayout:FrameLayout

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.bridge_web_view, container, false)
        webView = view.findViewById(R.id.webView)
        fullVideoLayout = view.findViewById(R.id.fullVideoLayout)
        
        webView.addOnH5EventListener(this)
        return view
    }

    override fun onH5Event(webView: BridgeExWebView, eventName: String, msg: String?, call: CallBackFunction) {
    }

    /**
     * 发送消息给h5
     */
    open fun sendEventToH5(event: String?, isCache: Boolean = false) {
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            webView.sendEventToH5("nativeCallWeb", event)
        } else {
            postToMainHandler{
                webView.sendEventToH5("nativeCallWeb", event)
            }
        }
    }

    /**
     * 开始加载链接
     */
    open fun loadUrl(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }
        webView.loadUrl(url)
    }
    
    open fun goBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            activity?.finishAfterTransition()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        webView.removeOnH5EventListener(this)
        webView.parent.asOrNull<ViewGroup>()?.removeView(webView)
        webView.stopLoading()
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        webView.settings.javaScriptEnabled = false
        //webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()

        webView.activity = null
        webView.fullLayout = null
        super.onDestroy()
    }
}