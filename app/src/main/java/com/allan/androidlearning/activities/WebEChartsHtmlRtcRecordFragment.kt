package com.allan.androidlearning.activities

import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.au.module_android.Globals
import com.au.module_android.utils.UnzipHelper
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

@Deprecated("android兼容性存在问题会导致某些手机，当前进程所有WebView会被占用")
class WebEChartsHtmlRtcRecordFragment : MyBridgeFragment() {
    companion object {
        var unziped = false
    }

    private val unzipHelper = UnzipHelper()
    private val echartsCacheDir = Globals.app.cacheDir.path + "/echarts_record"

    private fun findIndexHtml() : String {
        val path = "$echartsCacheDir/index.html"
        return path
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unzip {
            lifecycleScope.launch {
                loadUrl("file://" + findIndexHtml())
            }
        }

        webView.registerHandler("sendVideoBlobBase64") {
                data, cb->
            logd { "allanlog get js base64 blob size: " + data.length }
            lifecycleScope.launchOnThread {
                cb.onCallBack("success: " + saveToCache(data))
            }
        }
    }

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

    private fun saveToCache(blobString:String) : String{
        val fixStr = blobString.split(',')[1]
        // 解码Base64字符串为字节数组
        val videoBytes: ByteArray = Base64.decode(fixStr, Base64.DEFAULT)

        // 写入MP4文件到设备存储
        val videoFile = File(Globals.app.cacheDir, "video_${millisToTime()}.mp4")
        try {
            FileOutputStream(videoFile).use { fos ->
                fos.write(videoBytes)
                fos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return videoFile.name
    }

    private fun unzip(cb:()->Unit) {
        if (unziped) {
            cb()
            return
        }

        lifecycleScope.launchOnThread {
            delay(50)
            unzipHelper.copyFromAssets(Globals.app.assets,
                "echarts_record",
                arrayOf(
                "echarts.js",
                "index.html",
                "recordRTC.min.js",
                "html2canvas.min.js"), echartsCacheDir)
            delay(10)
            unziped = true
            cb()
        }
    }
}