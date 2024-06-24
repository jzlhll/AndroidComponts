package com.github.lzyzsd.jsbridgeex

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.github.lzyzsd.jsbridge.BridgeExWebView
import com.github.lzyzsd.jsbridge.BridgeUtil
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.R

/**
 * @author au
 * @date :2024/1/19 14:50
 * @description: use as Fragment
 */
open class BridgeWebViewExFragment : Fragment(), BridgeExWebView.OnH5EventListener {
    lateinit var webView:BridgeExWebView
    lateinit var root:ViewGroup
    lateinit var fullVideoLayout: ViewGroup

    /**
     * 参考bridge_web_view.xml，保留webView和fullVideoLayout。
     */
    open fun bridgeWebViewLayoutId() = R.layout.bridge_web_view

    open fun customUserAgent(originalUA:String) : String{
        return originalUA
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(bridgeWebViewLayoutId(), container, false)
        root = view as ViewGroup
        webView = view.findViewById(R.id.webView)
        webView.settings.textZoom = 100 //禁止文字缩放
        webView.settings.userAgentString = customUserAgent(webView.settings.userAgentString)
        fullVideoLayout = view.findViewById(R.id.fullVideoLayout)
        webView.fullLayout = fullVideoLayout
        webView.activity = activity
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
            BridgeUtil.getMainHandler().post{
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

    inline fun <reified T> Any?.asOrNull(): T? {
        return if (this is T) {
            this
        } else {
            null
        }
    }
}