package com.au.module_android.toast

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.au.module.android.R
import com.au.module.android.databinding.LayoutToast1Binding
import com.au.module.android.databinding.LayoutToast2Binding
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList
import kotlin.math.max

private val dismissToastRunnable = Runnable {
    dismissToast()
}

private const val DROP_DOWN_TIME = 400L

private var toastIndex = AtomicLong(1)

private val toastAlreadyShownList by unsafeLazy { ArrayList<View?>(4) }

@Synchronized
private fun createToastBinding(view: ViewGroup, duration: Long, lineNumber:Int): ViewBinding? {
    if (view.parent == null || !view.isAttachedToWindow) {
        return null
    }
    val binding:ViewBinding = if (lineNumber == 1) {
        LayoutToast1Binding.inflate(LayoutInflater.from(view.context), view, true)
    } else {
        LayoutToast2Binding.inflate(LayoutInflater.from(view.context), view, true)
    }

    val toast = binding.root
    toast.tag = toastIndex.addAndGet(1)
    toast.invisible()
    toast.post {
        toast.translationY = -toast.height.toFloat()
        toast.visible()
        val y = 48f.dp
        toast.animate()
            .translationY(y)
            .setDuration(DROP_DOWN_TIME)
            .withEndAction {
                synchronized(toastAlreadyShownList) {
                    toastAlreadyShownList.add(toast)
                }
                //到位后直接清理掉非当前的。
                dismissToast(directlyRemove = true, clearCurrent = false)
            }
            .start()
    }

    view.postDelayed(dismissToastRunnable, duration)

    return binding
}

/**
 * 消除一个toast。是否直接清除或者动画退场。
 */
fun dismissToast(toast:View?, directly:Boolean) {
    toast?:return
    if (directly) {
        toast.parent?.asOrNull<ViewGroup>()?.removeView(toast)
    } else {
        toast.parent?.asOrNull<ViewGroup>()?.let {
            toast.animate()
                .translationY(-toast.height.toFloat())
                .setDuration(200)
                .withEndAction {
                    it.removeView(toast)
                }
                .start()
        }
    }
}

/**
 * 取消toast
 */
private fun dismissToast(directlyRemove:Boolean = false, clearCurrent:Boolean = true) {
    synchronized(toastAlreadyShownList) {
        var sz = toastAlreadyShownList.count()
        while (sz-- > 0) {
            val toast = toastAlreadyShownList[sz]
            if (clearCurrent) {
                dismissToast(toast, directlyRemove)
                toastAlreadyShownList[sz] = null
            } else if (toast?.tag != toastIndex.get()) {
                dismissToast(toast, directlyRemove)
                toastAlreadyShownList[sz] = null
            }
        }

        toastAlreadyShownList.removeAll { it == null }
    }
}

private fun iconStrToId(icon:String?) = when(icon) {
    "success"-> R.drawable.ic_successful
    "fail", "error" -> R.drawable.ic_failure
    "warn" -> R.drawable.ic_warning
    else -> -1
}

private fun toastPopup(view: ViewGroup, duration: Long, message:String?, description:String?, icon:String?): ViewBinding? {
    val msg:String
    val desc:String?
    if (message != null && description != null) {
        msg = message
        desc = description
    } else if (message == null && description != null) {
        msg = description
        desc = null
    } else if (message != null) { // && description == null
        msg = message
        desc = null
    } else { // else if (message == null && description == null)
        msg = ""
        desc = null
    }

    val lineNumber = if (desc == null) 1 else 2
    val binding = createToastBinding(view, duration, lineNumber)

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
        }
    }
    return binding
}

fun toastOnTop(string: String, desc:String? = null, @IconType icon:String? = null, duration: Long = 2200) =
    ToastBuilder().setOnTop().setMessage(string).setDesc(desc).setIcon(icon).setDuration(duration).toast()

class ToastBuilder {
    private var decorView:ViewGroup? = null
    private var mMsg:String? = null //如果有desc，则这是标题；如果没有desc就是它一行
    private var mDesc:String? = null //如果有msg，则这个是第二行；如果没有msg，则就是它一行
    private var mIcon:String? = null //图标: success, fail, warn, none, null
    private var mDuration:Long = 2200 //时长
    private var mAlwaysShown = false //一直显示

    /**
     * 其一：从Activity中调用
     */
    fun setOnActivity(activity: Activity?) : ToastBuilder {
        decorView = activity?.window?.decorView.asOrNull()
        return this
    }
    /**
     * 其一：从Fragment中调用
     */
    fun setOnFragment(fragment: Fragment) : ToastBuilder {
        decorView = fragment.activity?.window?.decorView.asOrNull()
        return this
    }

//    fun setOnDialogFragment(fragment: Fragment) : DefaultToastBuilder{
//        val dialog = BottomSheetDialog.findBottomSheetDialog(fragment)
//        decorView = dialog?.findToastViewGroup()
//        return this
//    }

    /**
     * 其一：从最顶中调用
     */
    fun setOnTop() : ToastBuilder {
        setOnActivity(Globals.activityList.lastOrNull())
        return this
    }

    /**
     * 其一：从次顶调用
     */
    fun setOnSecondTop() : ToastBuilder {
        val list = Globals.activityList
        setOnActivity(list[max(0, list.size - 2)])
        return this
    }

    fun setMessage(msg:String?) : ToastBuilder {
        mMsg = msg
        return this
    }

    fun setDesc(desc:String?) : ToastBuilder {
        mDesc = desc
        return this
    }

    fun setIcon(@IconType icon:String?) : ToastBuilder {
        mIcon = icon
        return this
    }

    fun setDuration(duration: Long) : ToastBuilder {
        mDuration = duration
        return this
    }

    fun setAlwaysShown(shown:Boolean) : ToastBuilder {
        mAlwaysShown = shown
        return this
    }

    fun toast() : View? {
        return if(decorView == null) null else toastPopup(decorView!!, mDuration, mMsg, mDesc, mIcon)?.root
    }
}