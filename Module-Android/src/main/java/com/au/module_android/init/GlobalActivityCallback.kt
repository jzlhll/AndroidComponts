package com.au.module_android.init

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.au.module_android.Globals.activityList
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.logdNoFile

/**
 * @author au
 * Date: 2023/8/18
 * Description 全局监听activity变化
 */
class GlobalActivityCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        //logdNoFile { "onActivityCreated $activity ${activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName}" }
        activityList.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        val size = activityList.size
        val index = activityList.indexOf(activity)
        if (index >= 0) {
            activityList[index] = activityList[size - 1]
            activityList[size - 1] = activity
        }
        logdNoFile {
            val n = activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName ?: ""
            "onActivityStarted $activity $n" }
    }

    override fun onActivityResumed(activity: Activity) {
        //logd { "onActivityResumed $activity ${activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName}" }
    }

    override fun onActivityPaused(activity: Activity) {
        //logd { "onActivityPaused $activity ${activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName}" }
        //往下移动一层
    }

    override fun onActivityStopped(activity: Activity) {
        logdNoFile {
            val n = activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName ?: ""
            "onActivityStopped $activity $n"
        }
        //onConfigChange 会导致混乱。某些手机在切换darkMode和lightMode的时候，多层Activity会错乱。
        // 如果在最上面的activity出现了onStop，就证明他主动退让了一层。我们就处理一下
        val size = activityList.size
        if (size > 1 && activityList[size - 1] == activity) {
            activityList[size - 1] = activityList[size - 2]
            activityList[size - 2] = activity
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityList.remove(activity)
        //logdNoFile { "onActivityDestroyed $activity ${activity.asOrNull<FragmentShellActivity>()?.fragmentClass?.simpleName}" }
    }
}