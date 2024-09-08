package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.R
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.utils.logd
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.launch

@EntroFrgName
class WebVideoPlayFragment : MyBridgeFragment() {
    override fun bridgeWebViewLayoutId(): Int {
        logd { "aaa" }
        return R.layout.bridge_web_view_play
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.findViewById<Button>(R.id.startEchartsHtmlBtn).onClick {
            FragmentRootActivity.start(requireContext(), WebEChartsHtmlRtcRecordFragment::class.java)
        }

        root.findViewById<Button>(R.id.startEchartsSoftWebViewBtn).onClick {
            FragmentRootActivity.start(requireContext(), WebEChartsWebViewSoftRecordFragment::class.java)
        }

        root.findViewById<Button>(R.id.startEchartsBitmapBtn).onClick {
            FragmentRootActivity.start(requireContext(), WebEChartsGetDataUrlBitmapFragment::class.java)
        }

        lifecycleScope.launch {
            webView.loadUrl("https://v.qq.com/x/cover/mzc00200zi9e1k6/v3311o0ash6.html")
        }
    }
}