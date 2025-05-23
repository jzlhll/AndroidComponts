package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.R
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.logd
import kotlinx.coroutines.launch

@EntryFrgName
class WebVideoPlayFragment : MyBridgeFragment() {
    override fun bridgeWebViewLayoutId(): Int {
        logd { "aaa" }
        return R.layout.bridge_web_view_play
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.findViewById<Button>(R.id.startEchartsHtmlBtn).onClick {
            FragmentShellActivity.start(requireContext(), WebEChartsHtmlRtcRecordFragment::class.java)
        }

        root.findViewById<Button>(R.id.startEchartsSoftWebViewBtn).onClick {
            FragmentShellActivity.start(requireContext(), WebEChartsWebViewSoftRecordFragment::class.java)
        }

        root.findViewById<Button>(R.id.startEchartsBitmapBtn).onClick {
            FragmentShellActivity.start(requireContext(), WebEChartsGetDataUrlBitmapFragment::class.java)
        }

        lifecycleScope.launch {
            webView.loadUrl("https://v.qq.com/x/cover/mzc00200zi9e1k6/v3311o0ash6.html")
        }
    }
}