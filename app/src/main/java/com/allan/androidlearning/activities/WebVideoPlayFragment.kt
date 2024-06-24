package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.R
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.utils.UnzipHelper
import com.au.module_android.utils.launchOnThread
import com.github.lzyzsd.jsbridgeex.BridgeWebViewExFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WebVideoPlayFragment : BridgeWebViewExFragment() {
    private val videoPath = Globals.app.cacheDir.absolutePath + "/videos"
    companion object {
        var unziped = false
    }

    override fun bridgeWebViewLayoutId(): Int {
        return R.layout.bridge_web_view_play
    }

    private val unzipHelper = UnzipHelper()
    private fun findIndexHtml() : String {
        val path = "$videoPath/video.html"
        return path
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

        unzip {
            lifecycleScope.launch {
               // webView.loadUrl("file://" + findIndexHtml())
                webView.loadUrl("https://v.qq.com/x/cover/mzc00200zi9e1k6/v3311o0ash6.html")
            }
        }
    }

    private fun unzip(cb:()->Unit) {
        if (unziped) {
            cb()
            return
        }

        lifecycleScope.launchOnThread {
            delay(50)
            unzipHelper.copyFromAssets(Globals.app.assets,
                "video",
                arrayOf(
                    "video.html"), videoPath)
            delay(10)
            unziped = true
            cb()
        }
    }
}