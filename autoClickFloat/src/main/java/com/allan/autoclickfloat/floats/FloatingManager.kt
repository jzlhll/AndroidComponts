package com.allan.autoclickfloat.floats

import android.util.Log
import com.allan.autoclickfloat.floats.bean.ACTION_REMOVE
import com.allan.autoclickfloat.floats.bean.ACTION_SHOW
import com.allan.autoclickfloat.floats.bean.ACTION_START
import com.allan.autoclickfloat.floats.bean.ACTION_STOP
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.allan.autoclickfloat.floats.views.FloatingSettingView
import com.allan.autoclickfloat.floats.views.FloatingStepView
import com.allan.autoclickfloat.floats.views.WindowMgr
import com.au.module_android.simplelivedata.NoStickLiveData

/**
 * @author allan
 * @date :2024/4/15 16:51
 * @description:
 */
object FloatingManager {
    var enableAutoClick = false
        private set

    /** 更新自动点击的liveData
     */
    val autoClickLiveData = NoStickLiveData<AutoClickInfo>()
    private var isInited = false

    fun init() {
        if (isInited) return
        isInited = true
        autoClickLiveData.observeForeverUnStick { info->
            info!!
            Log.d("allan", "autoClick LvieData $info")
            when (info.action) {
                ACTION_SHOW -> {
                    WindowMgr.floatingStep = WindowMgr.floatingStep ?: FloatingStepView().also { it.mNotAlpha = 0.6f }
                    WindowMgr.floatingStep?.show()

                    WindowMgr.floatingSetting = WindowMgr.floatingSetting ?: FloatingSettingView()
                    WindowMgr.floatingSetting?.show()
                }

                ACTION_REMOVE -> {
                    WindowMgr.floatingSetting?.remove()
                    WindowMgr.floatingStep?.remove()
                    enableAutoClick = false
                }

                ACTION_START -> {
                    enableAutoClick = true
                }

                ACTION_STOP -> {
                    enableAutoClick = false
                }
            }
        }
    }

    var currentStep = 1

    fun saveCurrentStep() {

    }
}