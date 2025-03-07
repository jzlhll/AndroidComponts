package com.allan.autoclickfloat.activities.autofs

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.au.module_android.Globals
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.utils.startOutActivity
import com.au.module_android.widget.CustomFontText
import com.au.module_cached.AppDataStore

class KeepScreenOnFragment : ViewFragment() {
    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().apply {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (!AutoFsObj.isSwitchOnce()) {
            logd { "allanAlarm set next alarm!!!" }
            AutoFsObj.startAlarmWhenLoop(requireContext())
        } else {
            AppDataStore.remove<Long>("autoFsTargetTs")
        }

        Globals.mainHandler.postDelayed({
            logd { "allanAlarm delay do launch!!!" }
            val context = requireActivity()
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
            if (intent != null) {
                logd { "allanAlarm delay toast lanch!!!" }
                Toast.makeText(context, "launch apk ...", Toast.LENGTH_SHORT).show()
                context.startOutActivity(intent)
            } else {
                logd { "allanAlarm delay toast no lanch!!!" }
                Toast.makeText(context, "No target apk!", Toast.LENGTH_SHORT).show()
            }

            requireActivity().finish()
        }, 8 * 1000)
        return LinearLayout(inflater.context).also {
            it.addView(CustomFontText(inflater.context).also { tv->
                tv.text = "Alarm!!!"
                tv.textSize = 20f.dp
                tv.gravity = Gravity.CENTER
                tv.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            })
        }
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