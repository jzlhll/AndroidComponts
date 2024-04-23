package com.allan.autoclickfloat.activities.projects

import android.util.Log
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.bean.ACTION_REMOVE
import com.allan.autoclickfloat.floats.bean.ACTION_SHOW
import com.allan.autoclickfloat.floats.bean.ACTION_START
import com.allan.autoclickfloat.floats.bean.ACTION_STOP
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.allan.autoclickfloat.floats.views.FloatingSettingView
import com.allan.autoclickfloat.floats.views.FloatingStepView
import com.au.module_android.simplelivedata.NoStickLiveData

/**
 * @author allan
 * @date :2024/4/15 16:51
 * @description:
 */
object FloatingManager {
    var floatingSetting: FloatingSettingView? = null
    var floatingStep: FloatingStepView? = null

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
            Log.d(Const.TAG, "autoClick LvieData $info")
            when (info.action) {
                ACTION_SHOW -> {
                    floatingStep = floatingStep ?: FloatingStepView().also { it.mNotAlpha = 0.6f }
                    //todo floatingStep?.show()

                    floatingSetting = floatingSetting ?: FloatingSettingView()
                    //todo  floatingSetting?.show()
                }

                ACTION_REMOVE -> {
                    floatingSetting?.remove()
                    floatingStep?.remove()
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