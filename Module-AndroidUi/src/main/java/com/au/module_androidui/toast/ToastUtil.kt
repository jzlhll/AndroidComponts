package com.au.module_androidui.toast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module.android.R
import com.au.module_android.click.onClick
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import com.au.module_android.toast.AbsToastBuilder
import com.au.module_android.toast.IconType
import com.au.module_android.toast.ToastUtil
import com.au.module_android.toast.ToastUtil.checkToastMsgAndDesc
import com.au.module_android.toast.ToastUtil.dismissToastByTag
import com.au.module_androidui.databinding.LayoutToast1Binding
import com.au.module_androidui.databinding.LayoutToast2Binding
import kotlin.math.max

private fun createToastBinding(view: ViewGroup,
                               duration: Long,
                               lineNumber:Int,
                               textLen:Int,
                               isAlwaysShown:Boolean = false): ViewBinding {
    val viewBinding = if (lineNumber == 1) {
        LayoutToast1Binding.inflate(LayoutInflater.from(view.context), view, true)
    } else {
        LayoutToast2Binding.inflate(LayoutInflater.from(view.context), view, true)
    }

    return ToastUtil.createToastBinding(view, viewBinding, duration, textLen, isAlwaysShown)
}

private fun toastPopup(view: ViewGroup?, duration: Long, message:String?, description:String?, icon:String?,
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
        is LayoutToast1Binding -> {
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

        is LayoutToast2Binding -> {
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
                dismissToastByTag(tag)
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

fun toastOnTop(msg:String, desc:String? = null, @IconType icon:String?= null, duration:Long = 2200)
        = ToastBuilder().setOnTop().setMessage(msg).setDesc(desc).setIcon(icon).setDuration(duration).toast()

class ToastBuilder : AbsToastBuilder() {
    override fun toastPopup(): View? {
        return toastPopup(decorView, mDuration, mMsg, mDesc, mIcon, mAlwaysShown, mHasClose)?.root
    }
}