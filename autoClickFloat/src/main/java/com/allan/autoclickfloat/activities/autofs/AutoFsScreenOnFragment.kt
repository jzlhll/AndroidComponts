package com.allan.autoclickfloat.activities.autofs

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.allan.autoclickfloat.activities.autofs.spider.SpiderFragment
import com.allan.autoclickfloat.activities.autofs.spider.checkhtml.AbstractCheckHtml
import com.allan.autoclickfloat.activities.autofs.spider.checkhtml.CheckHtmlArgs
import com.allan.autoclickfloat.activities.autofs.spider.filter.DnSpiderWebViewFilter
import com.allan.autoclickfloat.databinding.FragmentFsScreenOnBinding
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.myHideSystemUI
import com.au.module_android.utils.startOutActivity
import com.modulenative.AppNative

class AutoFsScreenOnFragment : BindingFragment<FragmentFsScreenOnBinding>() {
    private var jump = true

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        requireActivity().apply {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            myHideSystemUI()
        }

        Globals.mainHandler.post {
            binding.fragContainerId.getFragment<SpiderFragment?>()?.webView?.let { spiderWebView->
                spiderWebView.filter = DnSpiderWebViewFilter()
                spiderWebView.checkHtml = object : AbstractCheckHtml() {
                    override fun checkHtml(html: String): CheckHtmlArgs {
                        val autoFsDot = "autofs."
                        if (html.contains(autoFsDot)) {
                            val index = html.indexOf(autoFsDot) + autoFsDot.length
                            val nextDotIndex = html.indexOf(".", index)
                            val moreStr = html.substring(index, nextDotIndex)
                            logdNoFile { moreStr }
                            if (moreStr == "disable") {
                                jump = false
                            }
                        }
                        return CheckHtmlArgs(false)
                    }
                }
                spiderWebView.loadUrl("https://blog.csdn.net/jzlhll123/article/details/145456286")
            }
        }

        AutoFsObj.checkAndStartNextAlarm(requireContext())

        Globals.mainHandler.postDelayed({
            delayWork()
        }, 8 * 1000)
    }

    private fun delayWork() {
        logd { "allanAlarm delay do launch!!! jump $jump" }
        if (jump) {
            val context = Globals.app
            val pm = context.packageManager

            val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
            if (intent != null) {
                logd { "allanAlarm delay toast lanch!!!" }
                context.startOutActivity(intent)
            } else {
                logd { "allanAlarm delay toast no lanch!!!" }
            }
        }

        activity?.finish()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        val ac = requireActivity()
        val km = ac.getSystemService(Context.KEYGUARD_SERVICE) as (KeyguardManager)
        km.requestDismissKeyguard(ac, object : KeyguardManager.KeyguardDismissCallback() {
            override fun onDismissCancelled() {
                super.onDismissCancelled()
                logd { "allan-alarm onDismissCancelled" }
            }

            override fun onDismissSucceeded() {
                super.onDismissSucceeded()
                logd { "allan-alarm onDismissSucceeded" }
            }

            override fun onDismissError() {
                super.onDismissError()
                logd { "allan-alarm onDismissError" }
            }
        })
    }

//        val wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager
//        if (wifiManager.isWifiEnabled) {
//            logd { "allanAlarm hasWifi..." }
//        } else {
//            logd { "allanAlarm no wifi..." }
////            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
////            startActivityFix(panelIntent)
//          //android12失效  wifiManager.setWifiEnabled(true)
//            // 直接修改系统设置 需要系统权限
////            Settings.Global.putInt(requireContext().contentResolver,  "wifi_on", 1);
//        }
}