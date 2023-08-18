package com.au.module_android.toast

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

private var toastIndex = AtomicLong(1)
private val call = Runnable {
    dismissToast()
}

/**
 * 已经上屏的Toast list。注：不包括永远显示的，永远显示的需要自行管理消除。
 */
private val toastAlreadyStickList = ArrayList<View?>()

//isAlwaysShown表示是否常驻，则没有delay关闭的事情。则需要自行根据返回值，自行关闭，本类不做管理。
//这里是创建布局，用于layout共享了icon，所以这里不需要icon
@Synchronized
private fun createToastBinding(view: ViewGroup?, duration: Long, lineNumber:Int,
                               isAlwaysShown:Boolean = false): ViewBinding? {
    view ?: return null
    //view不存在，则不处理
    if (view.parent == null || !view.isAttachedToWindow) {
        return null
    }
    val binding:ViewBinding = if (lineNumber == 1) {
        DialogTyphurToastPopup1Binding.inflate(LayoutInflater.from(view.context), view, true)
    } else {
        DialogTyphurToastPopup2Binding.inflate(LayoutInflater.from(view.context), view, true)
    }

    val toast = binding.root
    toast.tag = toastIndex.addAndGet(1)
    toast.invisible()
    toast.post {
        toast.translationY = -toast.height.toFloat()
        toast.visible()
        var y = 48f.dp
        y += globalToastHeight
        toast.animate()
            .translationY(y)
            .setDuration(400)
            .withEndAction {
                if (!isAlwaysShown) {
                    //当到位了以后才添加到列表。
                    synchronized(toastAlreadyStickList) {
                        toastAlreadyStickList.add(toast)
                    }
                }

                //到位后直接清理掉非当前的。
                dismissToast(directlyRemove = true, clearCurrent = false)
            }
            .start()
    }
    if (!isAlwaysShown) {
        view.removeCallbacks(call)
        view.postDelayed(call, duration)
    }

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
    synchronized(toastAlreadyStickList) {
        var sz = toastAlreadyStickList.count()
        while (sz-- > 0) {
            val toast = toastAlreadyStickList[sz]
            if (clearCurrent) {
                dismissToast(toast, directlyRemove)
                toastAlreadyStickList[sz] = null
            } else if (toast?.tag != toastIndex.get()) {
                dismissToast(toast, directlyRemove)
                toastAlreadyStickList[sz] = null
            }
        }

        toastAlreadyStickList.removeAll { it == null }
    }
}

private fun iconStrToId(icon:String?) = when(icon) {
    "success"-> R.drawable.toast_success
    "fail", "error" -> R.drawable.toast_error
    "warn" -> R.drawable.toast_warn
    else -> -1
}

fun toastPopup(view: ViewGroup?, duration: Long, message:String?, description:String?, icon:String?, isAlwaysShown: Boolean = false): ViewBinding? {
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
    val binding = createToastBinding(view, duration, lineNumber, isAlwaysShown=isAlwaysShown)

    when (binding) {
        is DialogTyphurToastPopup1Binding -> {
            binding.text.text = msg
            val iconId = iconStrToId(icon)
            if (iconId > 0) {
                binding.icon.setImageResource(iconId)
                binding.icon.visible()
            } else {
                binding.icon.gone()
            }
        }

        is DialogTyphurToastPopup2Binding -> {
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

//---------------------以前的写法，start

fun Fragment.toast(msg: String?, duration: Long = 2200, desc:String? = null) =
    toastPopup(appCompatActivity.window.decorView.asOrNull(), duration, msg, desc, null)

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
    BaseGlobalConst.activityList.lastOrNull()?.toast(strId, duration)
/**
 * 全局弹出toast，在最上面的activity上。
 */
fun toastOnTop(msg: String?, duration: Long = 2200, desc:String? = null) =
    BaseGlobalConst.activityList.lastOrNull()?.toast(msg, duration, desc)
//---------------------end

//////全新写法使用Builder模式

class TyphurToastBuilder {
    private var decorView:ViewGroup? = null
    private var mMsg:String? = null //如果有desc，则这是标题；如果没有desc就是它一行
    private var mDesc:String? = null //如果有msg，则这个是第二行；如果没有msg，则就是它一行
    private var mIcon:String? = null //图标: success, fail, warn, none, null
    private var mDuration:Long = 2200 //时长
    private var mAlwaysShown = false //一直显示

    /**
     * 其一：从Activity中调用
     */
    fun setOnActivity(activity: Activity?) : TyphurToastBuilder{
        decorView = activity?.window?.decorView.asOrNull()
        return this
    }
    /**
     * 其一：从Fragment中调用
     */
    fun setOnFragment(fragment: Fragment) : TyphurToastBuilder{
        decorView = fragment.appCompatActivity.window.decorView.asOrNull()
        return this
    }

    fun setOnDialogFragment(fragment: Fragment) : TyphurToastBuilder{
        val dialog = TyphurBottomSheetDialog.findTyphurBottomSheetDialog(fragment)
        decorView = dialog?.findToastViewGroup()
        return this
    }

    /**
     * 其一：从最顶中调用
     */
    fun setOnTop() : TyphurToastBuilder {
        setOnActivity(BaseGlobalConst.activityList.lastOrNull())
        return this
    }

    /**
     * 其一：从次顶调用
     */
    fun setOnSecondTop() : TyphurToastBuilder {
        val list = BaseGlobalConst.activityList
        setOnActivity(list[max(0, list.size - 2)])
        return this
    }

    fun setMessage(msg:String?) : TyphurToastBuilder {
        mMsg = msg
        return this
    }

    fun setDesc(desc:String?) : TyphurToastBuilder {
        mDesc = desc
        return this
    }

    fun setIcon(@IconType icon:String?) : TyphurToastBuilder {
        mIcon = icon
        return this
    }

    fun setDuration(duration: Long) : TyphurToastBuilder {
        mDuration = duration
        return this
    }

    fun setAlwaysShown(shown:Boolean) : TyphurToastBuilder {
        mAlwaysShown = shown
        return this
    }

    fun toast() : View? {
        return toastPopup(decorView, mDuration, mMsg, mDesc, mIcon, mAlwaysShown)?.root
    }
}