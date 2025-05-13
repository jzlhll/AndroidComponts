package com.au.module_android.init

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * 这是一个基础类，用于监听那些我们`感兴趣`的activity。只要其中任意一个活着，
 * 我们就认为本逻辑触发开始回调onLifeOpen。
 * 只有当所有`感兴趣`的activity都退出以后，才会触发onLifeClose。
 *
 * 适用于有多个界面共享某个逻辑的场景。
 */
abstract class InterestActivityCallbacks : Application.ActivityLifecycleCallbacks, IInterestLife{
    val scope = MainScope()
    private var lifeCount = 0

    /**
     * 是不是我们需要关注的activity。
     */
    abstract fun isLifeActivity(activity: Activity) : Boolean

    final override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    final override fun onActivityDestroyed(activity: Activity) {
    }

    final override fun onActivityPaused(activity: Activity) {
    }

    final override fun onActivityResumed(activity: Activity) {
    }

    final override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    final override fun onActivityStarted(activity: Activity) {
        if (isLifeActivity(activity)) {
            lifeCount++
            onLifeOpenEach()
            if (lifeCount == 1) {
                onLifeOpen()
            }
        }
    }

    final override fun onActivityStopped(activity: Activity) {
        if (isLifeActivity(activity)) {
            lifeCount--
            if (lifeCount == 0) {
                onLifeClose()

                scope.cancel()
            }
        }
    }
}