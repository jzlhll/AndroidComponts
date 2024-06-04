package com.allan.autoclickfloat.nongyao

import android.util.Log
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.accessibility.AutoClickFloatAccessService
import com.allan.autoclickfloat.accessibility.AbsAccessServiceObserver
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module_android.Apps
import com.au.module_android.utils.launchOnThread
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2024/6/3 20:27
 * @description:
 */
class AppClickTasksObserver(service: AutoClickFloatAccessService) : AbsAccessServiceObserver(service) {
    private var isPaused = true

    private val mgr = AllNodesMgr()

    private var openObserver = Observer<Boolean> {
        Log.d(Const.TAG, "open ob $it")
        if (it) {
            AppClickTasksInfoView.getInstance().show(100, 200, WindowMgr.mWindowManager.defaultDisplay.rotation)
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

    private fun startWork() {
        Apps.mainScope.launch {

            mgr.start {

            }
        }
    }

    private fun stopWork() {
        mgr.stop()
    }
}