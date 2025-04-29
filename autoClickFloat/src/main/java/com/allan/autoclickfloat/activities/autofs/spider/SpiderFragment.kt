package com.allan.autoclickfloat.activities.autofs.spider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.au.module_android.ui.views.ViewFragment

class SpiderFragment : ViewFragment() {
    lateinit var webView: SpiderWebView

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FrameLayout(inflater.context).also {
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.addView(SpiderWebView(inflater.context).also { spiderWebView ->
                this.webView = spiderWebView
                spiderWebView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            })
        }
    }
}