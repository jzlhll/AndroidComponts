package com.allan.autoclickfloat.activities.autofs

import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
        logd { "delay do launch jump=$jump, " + isNetworkAvailable(requireActivity()) }
        if (jump) {
            val context = Globals.app
            val pm = context.packageManager

            val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
            if (intent != null) {
                logd { "delay toast launch success!!!" }
                context.startOutActivity(intent)
            } else {
                logd { "delay toast no launch!!!" }
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
        })
    }

    fun isNetworkAvailable(context: Context): String {
        try {
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return "Has TRANSPORT_WIFI"
                }
            }
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return "Has TRANSPORT_CELLULAR"
                }
            }

            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return "Has TRANSPORT_ETHERNET"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "No Network " + e.message
        }

        return "No Network!"
    }

}