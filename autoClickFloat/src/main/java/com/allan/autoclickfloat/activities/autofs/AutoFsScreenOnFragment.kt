package com.allan.autoclickfloat.activities.autofs

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import com.au.module_android.Globals
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.utils.myHideSystemUI
import com.au.module_android.utils.startOutActivity
import com.au.module_android.widget.CustomFontText
import com.module_native.AppNative

class AutoFsScreenOnFragment : ViewFragment() {
    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().apply {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            myHideSystemUI()
        }

        arguments?.remove("autoFsId")
        AutoFsObj.checkAndStartNextAlarm(requireContext())

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

        Globals.mainHandler.postDelayed({
            logd { "allanAlarm delay do launch!!!" }
            val context = Globals.app
            val pm = context.packageManager

            val originalStr = AppNative.simpleDecoder(intArrayOf(104,95,107,105,42,111,111,42,93,106,96,110,107,101,96,42,104,93,110,103,))
            val intent = pm.getLaunchIntentForPackage(originalStr)
            if (intent != null) {
                logd { "allanAlarm delay toast lanch!!!" }
                context.startOutActivity(intent)
            } else {
                logd { "allanAlarm delay toast no lanch!!!" }
            }
            activity?.finish()
        }, 8 * 1000)

        return LinearLayout(inflater.context).also {
            it.addView(CustomFontText(inflater.context).also { tv->
                tv.text = ""
                tv.textSize = 20f.dp
                tv.gravity = Gravity.CENTER
                tv.setBackgroundColor(Color.BLACK)
                tv.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            })
        }
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
}