package com.au.module_android.toast_stick

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.postToMainHandler
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.isMainThread

/**
 * @author allan.jiang
 * Date: 2023/8/14
 * Description 全局弹窗基础模板实现
 */
abstract class AbstractToastStick<T, VB:ViewBinding> {
    companion object {
        //用于标记这一个悬浮View的tag。如果有必要自行放开子类定制
        const val GLOBAL_VIEW_TOAST_FLAG = "tag_global_toast"

        //进入的下拉时间
        const val duration = 400L

        val defaultOffsetY = 48f.dp

        internal val globalToast = DefaultGlobalToast()

        val globalToastHeight:Int
            get() = globalToast.currentToastHeight
    }

    /**
     * 是否目前存在全局弹窗
     * 即，解析currentContext是否存在。
     */
    val isHasGlobalToast:Boolean
        get() = currentContext != null

    /**
     * 当前的弹窗信息内容
     */
    private var currentContext:T? = null

    /**
     * 上一次的全局toast类型。用来后续判断使用
     */
    var lastGlobalToastType:String? = null
        private set

    /**
     * 当消失了就是0，当显示了则会根据当前toast的高度来设置。
     */
    var currentToastHeight:Int = 0

    /**
     * 必须如此调用，这样就直接贴在decorView上。
     * XXXXBinding.inflate(inflater, decorView, true)
     */
    protected abstract fun createViewBinding(inflater:LayoutInflater, decorView: ViewGroup):VB

    //初始化点击事情，禁用动画，开启动画等等
    //设置content文字到布局上等等
    /**
     * @param binding 就是创建好了的ViewBinding
     * @param hasEnterAnim 指的是是否有往下掉落的动画
     * @param content 就是本次用来显示的数据。子类不需要自行找数据。
     */
    protected abstract fun afterInitViewBinding(binding: VB, hasEnterAnim: Boolean, content:T)

    private fun createToastBinding(activity: Activity, hasEnterAnim: Boolean, content:T): VB? {
        val decorViewGroup = activity.window.decorView.asOrNull<ViewGroup>() ?: return null
        val binding = createViewBinding(LayoutInflater.from(activity), decorViewGroup)

        val toast = binding.root

        toast.setOnClickListener {  } //全局置空点击事件；避免透出往下点击。如果有点击事件；子类二次设置即可。

        toast.tag = GLOBAL_VIEW_TOAST_FLAG
        if (hasEnterAnim) { //即表示有动画。
            toast.visibility = View.INVISIBLE
            toast.post {
                toast.translationY = -toast.height.toFloat()
                toast.visibility = View.VISIBLE
                toast.animate()
                    .translationY(defaultOffsetY)
                    .setDuration(duration)
                    .withEndAction {
                        currentToastHeight = toast.height //目前放在这里进行判断。新界面是一上来就直接OK了。View的高度
                    }
                    .start()
            }
        } else {
            toast.translationY = defaultOffsetY
        }

        afterInitViewBinding(binding, hasEnterAnim, content)

        return binding
    }

    /**
     * 消除全局弹窗。是否直接清除或者动画退场。
     * 接入到onStop里面
     *
     */
    internal fun onActivityStopped(activity:Activity) {
        dismissGlobalToastDirectly(activity, false)
    }

    /**
     * 接入到onStart显示的时候接入显示。这里会决策是否需要显示其他已经显示了的。这样是直接显示。
     */
    internal fun onActivityStarted(activity: Activity) {
        val info = currentContext
        if (info != null && findToast(activity) == null) {
            createToastBinding(activity, false, info)
        }
    }

    private fun findToast(activity: Activity): View? {
        val window = activity.window
        val decorView = window.decorView
        return decorView.findViewWithTag<ViewGroup>(GLOBAL_VIEW_TOAST_FLAG);
    }

    @Synchronized
    fun dismissGlobalToastDirectly(activity:Activity, clearData:Boolean) {
        lastGlobalToastType = null//清理数据
        if(clearData) currentContext = null
        val window = activity.window
        val decorView = window.decorView
        findToast(activity)?.let {
            decorView.asOrNull<ViewGroup>()?.removeView(it)
        }

        currentToastHeight = 0
    }

    @Synchronized
    fun dismissGlobalToast(activity:Activity, clearData:Boolean) {
        if (!isMainThread) {
            postToMainHandler {
                dismissGlobalToast(activity, clearData)
            }
        } else {
            lastGlobalToastType = null
            //清理数据
            if(clearData) currentContext = null

            val window = activity.window
            val decorView = window.decorView
            decorView.asOrNull<ViewGroup>()?.let { decorViewGroup->
                decorViewGroup.findViewWithTag<ViewGroup>(GLOBAL_VIEW_TOAST_FLAG)?.let { toast->
                    toast.setOnClickListener(null)
                    toast.animate()
                        .translationY(-toast.height.toFloat())
                        .setDuration(200)
                        .withEndAction { //必须主线程才能回调
                            decorViewGroup.removeView(toast)
                        }
                        .start()

                }
            }

            currentToastHeight = 0
        }
    }

    /**
     * 刚刚有新的提示需要显示：给一个当前最新的activity 弹窗一个新的弹窗。
     */
    @Synchronized
    fun toastGlobal(type:String?, activity: Activity, content:T) {
        dismissGlobalToastDirectly(activity, true)

        currentContext = content
        lastGlobalToastType = type
        createToastBinding(activity, true, content)
    }
}