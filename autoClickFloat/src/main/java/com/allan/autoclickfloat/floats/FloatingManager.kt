package com.allan.autoclickfloat.floats

import com.allan.autoclickfloat.floats.bean.ACTION_CLOSE
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
    private var enableAutoClick = false

    /** 更新自动点击的liveData
     */
    val autoClickLiveData = NoStickLiveData<AutoClickInfo>()

    init {
        autoClickLiveData.observeForeverUnStick {
            it!!
            when (it.action) {
                ACTION_SHOW -> {
                    WindowMgr.floatingSetting = WindowMgr.floatingSetting ?: FloatingSettingView()
                    WindowMgr.floatingSetting?.show()
                    WindowMgr.floatingStep = WindowMgr.floatingStep ?: FloatingStepView()
                    WindowMgr.floatingStep?.show()
                }

                ACTION_CLOSE -> {
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

        var currentStep = 1

        fun saveCurrentStep() {

        }
    }
}