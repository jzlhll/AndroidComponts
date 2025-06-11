package com.au.logsystem

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity

abstract class AllActivityFollowCallback : Application.ActivityLifecycleCallbacks {
    /**
     * 返回布局R.xml.xxx
     * 根布局必须是DragRelativeLayout
     */
    abstract val layoutRes:Int

    /**
     * 返回整个布局的根布局的id。所以要求你必须设置一个id给到根布局。
     */
    abstract val rootId:Int

    /**
     * 显示的属性。是直接贴在decorView上。所以请给出FrameLayout.LayoutParams的显示方式
     */
    abstract fun showParams(): FrameLayout.LayoutParams

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if(!canShow(activity, savedInstanceState)) return
        showOnDecorView(activity)
    }

    private fun findShownOnDecorView(activity: Activity): View? {
        val decorView = activity.window.decorView as FrameLayout
        return decorView.findViewById(rootId)
    }

    /**
     * 显示的时候调用
     */
    open fun showOnDecorView(activity: Activity) {
        val findView = findShownOnDecorView(activity)
        if (findView == null) {
            val decorView = activity.window.decorView as FrameLayout
            val view = activity.layoutInflater.inflate(layoutRes, null)
            view.onClick {
                FragmentShellActivity.start(Globals.app, LogSystemFragment::class.java)
            }
            decorView.addView(view, showParams())
        }
    }

    /**
     * 不用显示的时候调用
     */
    open fun hideOnDecorView(activity: Activity, removeBlock : ()-> Unit) {
        val findView = findShownOnDecorView(activity)
        if (findView != null) {
            val decorView = activity.window.decorView as FrameLayout
            if (activity is AppCompatActivity && activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                findView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        decorView.removeView(findView)
                        removeBlock.invoke()
                    }
                    .start()
            } else {
                decorView.removeView(findView)
                removeBlock.invoke()
            }
        }
    }

    /**
     * 是否可以显示根据activity
     */
    abstract fun canShow(activity: Activity, savedInstanceState: Bundle?) : Boolean

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }
}