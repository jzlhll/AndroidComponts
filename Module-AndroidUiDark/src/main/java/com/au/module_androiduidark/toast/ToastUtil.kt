package com.au.module_androiduidark.toast

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.au.module.android.R
import com.au.module_android.Apps
import com.au.module_android.click.onClick
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import com.au.module_androidex.toast.AbsToastBuilder
import com.au.module_androidex.toast.ToastUtil
import com.au.module_androidex.toast.ToastUtil.checkToastMsgAndDesc
import com.au.module_androidex.toast.ToastUtil.dismissToastByTag
import com.au.module_androiduidark.databinding.LayoutToast1DarkBinding
import com.au.module_androiduidark.databinding.LayoutToast2DarkBinding
import kotlin.math.max

private fun createToastBinding(view: ViewGroup,
                               duration: Long,
                               lineNumber:Int,
                               textLen:Int,
                               isAlwaysShown:Boolean = false): ViewBinding {
    val viewBinding = if (lineNumber == 1) {
        LayoutToast1DarkBinding.inflate(LayoutInflater.from(view.context), view, true)
    } else {
        LayoutToast2DarkBinding.inflate(LayoutInflater.from(view.context), view, true)
    }

    return ToastUtil.createToastBinding(view, viewBinding, duration, textLen, isAlwaysShown)
}

fun toastPopup(view: ViewGroup?, duration: Long, message:String?, description:String?, icon:String?,
               isAlwaysShown: Boolean = false, hasClose: Boolean = false) : ViewBinding? {
    view ?: return null
    //view不存在，则不处理
    if (view.parent == null || !view.isAttachedToWindow) {
        return null
    }

    val pair = checkToastMsgAndDesc(message, description) ?: return null
    val msg = pair.first
    val desc = pair.second
    val maxTextLen = max(msg.length, desc?.length ?: 0)

    val lineNumber = if (desc == null) 1 else 2
    val binding = createToastBinding(view, duration, lineNumber, maxTextLen, isAlwaysShown = isAlwaysShown)
    val tag = binding.root.tag
    when (binding) {
        is LayoutToast1DarkBinding -> {
            binding.text.text = msg
            val iconId = iconStrToId(icon)
            if (iconId > 0) {
                binding.icon.setImageResource(iconId)
                binding.icon.visible()
            } else {
                binding.icon.gone()
            }

            if (hasClose) {
                binding.closeBtn.visible()
                binding.closeBtn.onClick {
                    dismissToastByTag(tag)
                }
            } else {
                binding.closeBtn.gone()
            }
        }

        is LayoutToast2DarkBinding -> {
            binding.text.text = msg
            binding.desc.text = desc
            val iconId = iconStrToId(icon)
            if (iconId > 0) {
                binding.icon.setImageResource(iconId)
                binding.icon.visible()
            } else {
                binding.icon.gone()
            }

            if (hasClose) {
                binding.closeBtn.visible()
            } else {
                binding.closeBtn.gone()
            }

            binding.closeBtn.onClick {
                ToastUtil.dismissToastByTag(tag)
            }
        }
    }
    return binding
}

private fun iconStrToId(icon:String?) = when(icon) {
    "success"-> R.drawable.ic_successful
    "fail", "error" -> R.drawable.ic_failure
    "alert", "warn", "tips", "info" -> R.drawable.ic_warning
    else -> -1
}

fun Fragment.toast(msg: String?, duration: Long = 2200, desc:String? = null) =
    toastPopup(requireActivity().window.decorView.asOrNull(), duration, msg, desc, null)

fun Fragment.toast(@StringRes strId: Int, duration: Long = 2200) = toast(getString(strId), duration)

fun Activity.toast(msg: String?, duration: Long = 2200, desc:String? = null) =
    toastPopup(window.decorView.asOrNull(), duration, msg, desc, null)

fun Activity.toast(@StringRes strId: Int, duration: Long = 2200) = toast(getString(strId), duration)

fun Window.toast(msg: String?, duration: Long = 2200, desc:String? = null) =
    toastPopup(this.decorView.asOrNull(), duration, msg, desc, null)

/**
 * 全局弹出toast，在最上面的activity上。
 */
fun toastOnTop(@StringRes strId: Int, duration: Long = 2200) =
    Apps.activityList.lastOrNull()?.toast(strId, duration)
/**
 * 全局弹出toast，在最上面的activity上。
 */
fun toastOnTop(msg: String?, duration: Long = 2200, desc:String? = null) =
    Apps.activityList.lastOrNull()?.toast(msg, duration, desc)

class ToastBuilder : AbsToastBuilder() {
    override fun toastPopup(): View? {
        return toastPopup(decorView, mDuration, mMsg, mDesc, mIcon, mAlwaysShown, mHasClose)?.root
    }
}