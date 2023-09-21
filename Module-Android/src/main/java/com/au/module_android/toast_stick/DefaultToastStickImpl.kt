package com.au.module_android.toast_stick

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.au.module.android.databinding.LayoutToastStickDemoBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.toast_stick.AbstractToastStick.Companion.globalToast
import com.au.module_android.utils.activity

fun dismissToastDirectlyGlobally(activity:Activity? = null) {
    val fixActivity = activity ?: Globals.activityList.lastOrNull() ?: return
    globalToast.dismissGlobalToastDirectly(fixActivity, true)
}

fun dismissToastGlobally(activity:Activity? = null) {
    val fixActivity = activity ?: Globals.activityList.lastOrNull() ?: return
    globalToast.dismissGlobalToast(fixActivity, true)
}

fun toastGlobal(type:String?, activity: Activity, message:String?, description:String?) {
    val msg:String
    val desc:String
    if (message != null && description != null) {
        msg = message
        desc = description
    } else if (message == null && description != null) {
        msg = ""
        desc = description
    } else if (message != null) { // && description == null
        msg = ""
        desc = message
    } else { // else if (message == null && description == null)
        msg = ""
        desc = ""
    }

    globalToast.toastGlobal(type, activity, TwoString(msg, desc))
}

val hasGlobalToast:Boolean
    get() = globalToast.isHasGlobalToast

val globalToastType:String?
    get() = if(globalToast.isHasGlobalToast) globalToast.lastGlobalToastType else null

/**
 * 刚刚有新的提示需要显示：给当前activity显示
 */
fun toastGlobalCurrent(type:String?, message:String?, description:String?) {
    val activity = Globals.activityList.lastOrNull() ?: return
    toastGlobal(type, activity, message, description)
}

//----------------------
data class TwoString(var first:String, var second:String)

class DefaultGlobalToast : AbstractToastStick<TwoString, LayoutToastStickDemoBinding>() {

    override fun createViewBinding(inflater: LayoutInflater, decorView: ViewGroup): LayoutToastStickDemoBinding {
        return LayoutToastStickDemoBinding.inflate(inflater, decorView, true)
    }

    override fun afterInitViewBinding(binding: LayoutToastStickDemoBinding, hasEnterAnim: Boolean, content: TwoString) {
        if (!hasEnterAnim) {
            //todo
        }

        binding.text.text = content.first

        binding.closeBtn.onClick { v->
            v.activity?.let { activity -> dismissGlobalToast(activity, true) }
        }
    }
}