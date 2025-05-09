package com.allan.androidlearning

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.au.module_android.Globals
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.views.ViewActivity
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.logdNoFile

class ShareReceiverActivity : ViewActivity() {
    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FrameLayout(inflater.context)
    }

    override fun onStart() {
        super.onStart()
        dealWithIntent(intent)
    }

    private fun dealWithIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                // 处理单文件分享
                val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                logdNoFile { "uri $uri" }
                if (uri != null) handleSharedUri(listOf(uri))
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // 处理多文件分享
                val uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                uris?.forEach { uri ->
                    logdNoFile { "uris uri $uri" }
                }
                uris?.let { handleSharedUri(it) }
            }
        }

        intent?.removeExtra(Intent.EXTRA_STREAM)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        dealWithIntent(intent)
    }

    private fun handleSharedUri(uri: List<Uri>) {
        val activityCount = Globals.activityList.size
        if (activityCount > 0) {
            //已经存在；直接发送
            val found = Globals.activityList.find { it.asOrNull<FragmentShellActivity>()?.fragmentClass == MyDroidTransferFragment::class.java }
            if (found != null) {
            } else {
                FragmentShellActivity.start(this, MyDroidTransferFragment::class.java, Bundle().also {
                    it.putParcelableArray("receiverUri", uri.toTypedArray())
                })
            }
        }

//        val pair = findLaunchActivity(context)
//        if (!pair.second) {
//            context.startActivityFix(pair.first.also {
//                it.putExtra("alarm", "alarmIsComingWhenNoStartActivity")
//            })
//        } else {
//            FragmentShellActivity.start(context, AutoFsScreenOnFragment::class.java)
//        }
    }
}