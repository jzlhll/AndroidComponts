package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.utils.ViewRecorder
import com.au.module_android.Globals
import com.au.module_android.utils.UnzipHelper
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.unsafeLazy
import com.github.lzyzsd.jsbridgeex.BridgeWebViewExFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat

class WebEChartsFragment : BridgeWebViewExFragment() {
    companion object {
        var unziped = false
    }

    private val unzipHelper = UnzipHelper()
    private val echartsCacheDir = Globals.app.cacheDir.path + "/echarts"

    private fun findIndexHtml() : String {
        val path = "$echartsCacheDir/showEcharts.html"
        return path
    }

    private val viewRecorder by unsafeLazy { ViewRecorder(webView) }

    fun millisToTime(): String {
        //首先获取当前的毫秒值
        val currentTimeMillis = System.currentTimeMillis()
        /**
         * 转换为年月日时分秒的形式,得到的currentTime就是转换之后的值了
         * yyyy-MM-dd HH-mm-ss
         * 年-月-日 时-分-秒
         */
        val currentTime: String = SimpleDateFormat("HHmmss").format(currentTimeMillis)
        return currentTime
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.registerHandler("webCallNative") { data, func->
            logd { "webCallNative $data hasCallback:" + (func != null) }
            when (data) {
                "initOver" -> {
                    viewRecorder.startRecording(File(Globals.app.cacheDir, "video_${millisToTime()}.mp4").absolutePath)
                    webView.sendEventToH5("nativeCallWeb", "startEcharts")
                }
                "runOver" -> {
                    viewRecorder.stopRecording()
                }
            }
        }
        unzip {
            lifecycleScope.launch {
                loadUrl("file://" + findIndexHtml())
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
                "echarts",
                arrayOf(
                "echarts.js",
                "showEcharts.html"), echartsCacheDir)
            delay(10)
            unziped = true
            cb()
        }
    }
}