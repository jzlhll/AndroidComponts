package com.au.module_android.init

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.au.module_android.Globals.activityList

/**
 * @author au
 * Date: 2023/8/18
 * Description 全局监听activity变化
 */
class GlobalActivityCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        //logd { "GlobalActivity Callback onActivityCreated $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
        activityList.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        val size = activityList.size
        if (size > 1 && activityList[size - 2] == activity) {
            activityList[size - 2] = activityList[size - 1]
            activityList[size - 1] = activity
        }
        //logd { "GlobalActivity Callback onActivityStarted $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
    }

    override fun onActivityResumed(activity: Activity) {
        //logd { "GlobalActivity Callback onActivityResumed $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
    }

    override fun onActivityPaused(activity: Activity) {
        //logd { "GlobalActivity Callback onActivityPaused $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
        //往下移动一层
    }

    override fun onActivityStopped(activity: Activity) {
        //logd { "GlobalActivity Callback onActivityStopped $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
        //onConfigChange 会导致混乱
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
        //logd { "GlobalActivity Callback onActivityDestroyed $activity ${activity.asOrNull<FragmentRootActivity>()?.fragmentClass?.simpleName}" }
    }
}