package com.allan.androidlearning.activities

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.ActivityEchartsBinding
import com.allan.androidlearning.recordview.ViewRecorderManager
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.UnzipHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.logt
import com.au.module_android.utils.unsafeLazy
import com.github.lzyzsd.jsbridge.BridgeWebViewExFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat

class WebEChartsWebViewSoftRecordFragment : BindingFragment<ActivityEchartsBinding>() {
    companion object {
        var unziped = false
    }

    private val unzipHelper = UnzipHelper()
    private val echartsCacheDir = Globals.app.cacheDir.path + "/echarts"

    private fun findIndexHtml() : String {
        val path = "$echartsCacheDir/showEcharts.html"
        return path
    }

    private val viewRecorder by unsafeLazy { ViewRecorderManager() }

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

    private lateinit var h5Fragment:RecordWebFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ignoreError {
        Globals.app.cacheDir.listFiles()?.forEach {
            if (it.isFile && it.name.startsWith("video_")) {
                it.delete()
            }
        } }

        requireActivity().title = "Demo View Record"

        val h5 = RecordWebFragment()
        parentFragmentManager.beginTransaction()
            .replace(binding.webViewHost.id, h5)
            .commitAllowingStateLoss()
        h5Fragment = h5

        h5.onCreatedCallback = {
            unzip {
                lifecycleScope.launch {
                    h5Fragment.loadUrl("file://" + findIndexHtml())
                }
            }

            h5.webView.registerHandler("webCallNative") { data, func->
                logt { ">>>webCallNative $data hasCallback:" + (func != null) }
                when (data) {
                    "initOver" -> {
                        val name = File(Globals.app.cacheDir, "video_${millisToTime()}.mp4").absolutePath
                        val suc = viewRecorder.setup(name, h5Fragment.webView)
                        binding.desc2Text.text = "generating $name ..."
                        logt { "setup $name $suc" }
                        if (suc) {
                            viewRecorder.startRecord()
                            h5Fragment.webView.sendEventToH5("nativeCallWeb", "startEcharts")
                        }
                    }
                    "runOver" -> {
                        if(viewRecorder.stopRecord()) {
                            val p = viewRecorder.curRecordFile?.absolutePath
                            logd { "toast suc! ${viewRecorder.curRecordFile}" }
                            binding.desc2Text.text = "$p suc!"
                            binding.desc2Text.setTextColor(Color.parseColor("#0c5199"))

                            val lp = binding.videoView.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()
                            lp?.matchConstraintPercentWidth = 0.78f
                            binding.videoView.layoutParams = lp

                            val videoUri = Uri.parse(p)
                            binding.videoView.setVideoURI(videoUri)
                            binding.videoView.start()
                        }
                    }
                }
            }
        }
    }

    private fun unzip(cb:()->Unit) {
        if (unziped) {
            cb()
            return
        }

        lifecycleScope.launchOnThread {
            unzipHelper.copyFromAssets(
                Globals.app.assets,
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

class RecordWebFragment : MyBridgeFragment() {
    var onCreatedCallback:()->Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        onCreatedCallback()
        return v
    }
}