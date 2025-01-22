package com.au.module_android.toast
import android.app.Activity
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.au.module_android.Globals
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.base.AbsFragment
import com.au.module_android.ui.base.IBaseDialog
import com.au.module_android.ui.base.findDialogByContentFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.invisible
import com.au.module_android.utils.visible
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

object ToastUtil {
    /**
     * dark和light模式两种，目前dark模式增加了close按钮。
     */
    private var toastIndex = AtomicLong(1)
    private val call = Runnable {
        dismissToast()
    }

    private var mLastInfo:String? = null
    private var mLastInfoTs = 0L
    private const val LastInfoDeltaTime = 1200L

    fun checkToastMsgAndDesc(message:String?, description:String?) : Pair<String, String?>? {
        val msg:String
        val desc:String?
        if (!message.isNullOrEmpty() && !description.isNullOrEmpty()) {
            msg = message
            desc = description
        } else if (message.isNullOrEmpty() && !description.isNullOrEmpty()) {
            msg = description
            desc = null
        } else if (!message.isNullOrEmpty()) { // && description == null
            msg = message
            desc = null
        } else { // else if (message == null && description == null)
            msg = ""
            desc = null
        }

        if (msg.isEmpty() && desc.isNullOrEmpty()) {
            return null
        }
        val info = msg + desc
        val curTs = SystemClock.elapsedRealtime()
        //稍微做一点重复的过滤。
        if (mLastInfo == info && curTs - mLastInfoTs <= LastInfoDeltaTime) {
            return null
        }
        mLastInfoTs = curTs
        mLastInfo = info
        return msg to desc
    }

    /**
     * 已经上屏的Toast list。注：不包括永远显示的，永远显示的需要自行管理消除。
     */
    private val toastAlreadyShownList = ArrayList<View?>()

    //isAlwaysShown表示是否常驻，则没有delay关闭的事情。则需要自行根据返回值，自行关闭，本类不做管理。
//这里是创建布局，用于layout共享了icon，所以这里不需要icon
    @Synchronized
    fun createToastBinding(view: ViewGroup,
                           binding:ViewBinding,
                           duration: Long,
                           textLen:Int,
                           isAlwaysShown:Boolean = false): ViewBinding {
        val toast = binding.root
        toast.tag = toastIndex.addAndGet(1)
        toast.invisible()
        toast.post {
            toast.translationY = -toast.height.toFloat()
            toast.visible()
            val y = 48f.dp
            toast.animate()
                .translationY(y)
                .setDuration(250)
                .withEndAction {
                    if (!isAlwaysShown) {
                        //当到位了以后才添加到列表。
                        synchronized(toastAlreadyShownList) {
                            toastAlreadyShownList.add(toast)
                        }
                    }

                    //到位后直接清理掉非当前的。
                    dismissToast(directlyRemove = true, clearCurrent = false)
                }
                .start()
        }
        if (!isAlwaysShown) {
            view.removeCallbacks(call)
            view.postDelayed(call, fixDuration(duration, textLen))
        }

        return binding
    }

    /**
     * 消除一个toast。是否直接清除或者动画退场。
     */
    private fun dismissToast(toast:View?, directly:Boolean) {
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

    fun dismissToastByTag(tag:Any?) {
        if (tag == null) return
        synchronized(toastAlreadyShownList) {
            var sz = toastAlreadyShownList.count()
            while (sz-- > 0) {
                val toast = toastAlreadyShownList[sz]
                if (toast != null && toast.tag == tag) {
                    dismissToast(toast, false)
                    toastAlreadyShownList[sz] = null
                    break
                }
            }

            toastAlreadyShownList.removeAll { it == null }
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

    private fun fixDuration(duration:Long, textLen:Int) : Long {
        if (duration > 2500 || duration < 1800) { //超过一定的范围，就认为你自行决策了时长。
            return duration
        }

        val fixDuration = (textLen * 55L + 500)
        return min(max(fixDuration, 1800), 3800)
    }
}

abstract class AbsToastBuilder {
    var decorView:ViewGroup? = null
    var mMsg:String? = null //如果有desc，则这是标题；如果没有desc就是它一行
    var mDesc:String? = null //如果有msg，则这个是第二行；如果没有msg，则就是它一行
    var mIcon:String? = null //图标: success, fail, warn, none, null
    var mDuration:Long = 2200 //时长
    var mAlwaysShown = false //一直显示
    var mHasClose = false //是否有关闭xx按钮
    var mLaterTs = 0L

    /**
     * 其一：从Activity中调用
     */
    fun setOnActivity(activity: Activity?) : AbsToastBuilder {
        decorView = activity?.window?.decorView.asOrNull()
        return this
    }
    /**
     * 其一：从Fragment中调用
     */
    fun setOnFragment(fragment: Fragment) : AbsToastBuilder {
        decorView = fragment.activity?.window?.decorView.asOrNull()
        return this
    }

    fun setOnFragmentDialog(contentFragment: AbsFragment) : AbsToastBuilder {
        decorView = contentFragment.findDialogByContentFragment().asOrNull<IBaseDialog>()?.findToastViewGroup()
        return this
    }

    fun setOnViewGroup(viewGroup: ViewGroup?) : AbsToastBuilder {
        decorView = viewGroup
        return this
    }

    /**
     * 其一：从最顶中调用
     */
    fun setOnTop() : AbsToastBuilder {
        setOnActivity(Globals.activityList.lastOrNull())
        return this
    }

    /**
     * 其一：delay执行
     */
    fun setOnTopLater(laterTs:Long = 1500) : AbsToastBuilder {
        mLaterTs = laterTs
        return this
    }

    fun setOnActivityByFragClass(clz : Class<*>): AbsToastBuilder {
        Globals.activityList.forEach {
            val activity = it.asOrNull<FragmentRootActivity>()
            if (activity?.fragmentClass == clz) {
                return setOnActivity(activity)
            }
        }
        return this
    }

    fun setMessage(msg:String?) : AbsToastBuilder {
        mMsg = msg
        return this
    }

    fun setDesc(desc:String?) : AbsToastBuilder {
        mDesc = desc
        return this
    }

    fun setIcon(@IconType icon:String?) : AbsToastBuilder {
        mIcon = icon
        return this
    }

    fun setDuration(duration: Long) : AbsToastBuilder {
        mDuration = duration
        return this
    }

    fun setAlwaysShown(shown:Boolean) : AbsToastBuilder {
        mAlwaysShown = shown
        return this
    }

    fun setHasClose(hasClose : Boolean) : AbsToastBuilder {
        mHasClose = hasClose
        return this
    }

    fun toast() : View? {
        if (decorView == null) {
            return null
        }
        return toastPopup()
    }

    protected abstract fun toastPopup() : View?
}