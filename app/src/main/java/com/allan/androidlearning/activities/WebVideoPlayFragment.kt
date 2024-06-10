package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.au.module_android.Globals
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

    private val unzipHelper = UnzipHelper()
    private fun findIndexHtml() : String {
        val path = "$videoPath/video.html"
        return path
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unzip {
            lifecycleScope.launch {
                webView.loadUrl("file://" + findIndexHtml())
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