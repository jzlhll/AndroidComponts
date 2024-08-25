package com.allan.autoclickfloat.nongyao

import android.content.ComponentName
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.accessibility.AbsAccessServiceObserver
import com.allan.autoclickfloat.accessibility.AutoClickFloatAccessService
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module.android.BuildConfig
import com.au.module_android.Globals
import com.au.module_android.utils.TAG
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2024/6/3 20:27
 * @description:
 */
class AppClickTasksObserver(service: AutoClickFloatAccessService) : AbsAccessServiceObserver(service) {
    private var isPaused = true

    private val mgr = AllNodesMgr()

    inline fun log(block:()->String) {
        if(BuildConfig.DEBUG) Log.d(TAG, "alland " + block())
    }

    private var openObserver = Observer<Boolean> {
        Log.d(Const.TAG, "open ob $it")
        if (it) {
            AppClickTasksInfoView.getInstance().show(100, 200, WindowMgr.mWindowManager.defaultDisplay.rotation)
            AppClickTasksInfoView.getInstance().updateInfo("开始...")
            startWork()
        } else {
            AppClickTasksInfoView.getInstanceOrNull()?.remove()
            stopWork()
        }
    }

    override fun onCreate() {
        Const.appClickTasks.openLiveData.observeForeverUnStick(openObserver)
    }

    override fun onDestroy() {
        Const.appClickTasks.openLiveData.removeObserverUnStick(openObserver)
    }

    override fun onScreenOff() {
        isPaused = true
        AppClickTasksInfoView.getInstance().apply {
            updateInfo("已经暂停，\n点击重新开始。")
            disableTouch = false
            clickCallback = {
                isPaused = false
                AppClickTasksInfoView.getInstance().disableTouch = true
                startWork()
            }
        }
    }

    override fun onScreenOn() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //当界面发生改变时，这个方法就会被调用，界面改变的具体信息就会包含在这个参数中。
        log { "-------" }
        val nodeInfo = event?.source //当前界面的可访问节点信息
        log { "a: $event" }
        log { "b: $nodeInfo" }
        nodeInfo?.findAccessibilityNodeInfosByViewId("发现")?.forEach {
            //it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            log { "c: ${it.viewIdResourceName}"  }
        }
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//界面变化事件
            //val componentName = ComponentName(event.packageName.toString(), event.className.toString())
            //val activityInfo = service.tryGetActivity(componentName)
            //val isActivity = activityInfo != null
            //log { "isActivity:$isActivity componentName $componentName $activityInfo" }
            service.getCurrentRootNode()?.findAccessibilityNodeInfosByViewId("发现")?.forEach {
                //it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                log { "viewIdResourceName1 ${it.viewIdResourceName}"  }
            }
        } else if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            // 这里处理点击事件
            log{"View clicked: " + event.className }
        }
    }

    private fun startWork() {
        Globals.mainScope.launch {
            val result = mgr.start {
                AppClickTasksInfoView.getInstance().updateInfo("进度$it%...")
            }
            AppClickTasksInfoView.getInstance().updateInfo(result)
        }
    }

    private fun stopWork() {
        mgr.stop()
    }
}