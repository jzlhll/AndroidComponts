package com.allan.autoclickfloat.activities.coverscreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.LayoutRes
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.Globals
import com.au.module_cached.AppDataStore
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2024/4/15 16:43
 * @description: 不要给任何的子View设置点击事件。通过直接监听这里面的clickCallback来处理
 */
class SmallenIconFloatingView(@LayoutRes layoutId:Int) : BaseFloatingView(layoutId) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance:SmallenIconFloatingView? = null
        fun getInstance() : SmallenIconFloatingView {
            if (instance == null) {
                instance = SmallenIconFloatingView(R.layout.view_floating_over_screen_smallen)
            }
            return instance!!
        }

        fun getInstanceOrNull() = instance
    }

    init {
        touchUpCallback = {x, y->
            Log.d(Const.TAG, "autoClick saved x=$x, y=$y")
            savePoint(x, y, Const.rotationLiveData.value!!)
        }

        clickCallback = {
            CoverScreenFloatingView.getInstance().show(0, 0, Const.rotationLiveData.value!!)
            getInstanceOrNull()?.remove()
        }

        mNotAlpha = 0.5f
    }

    fun savePoint(x:Int, y:Int,  rotation:Int) {
        Log.d(Const.TAG, "saveAutoOnePoint rotation $rotation x-y $x $y")
        Globals.mainScope.launch {
            AppDataStore.save(
                "smallenIconFloatX" to x,
                "smallenIconFloatY" to y,
                "smallenIconFloatRotation" to rotation)
        }
    }

    fun loadShow() {
        if (isShown) {
            return
        }
        val x = AppDataStore.readBlocked("smallenIconFloatX", 100)
        val y = AppDataStore.readBlocked("smallenIconFloatY", 400)
        val rotation = AppDataStore.readBlocked("smallenIconFloatRotation", -1)
        val fixRotation = if(rotation == -1) Const.rotationLiveData.value!! else rotation
        show(x, y, fixRotation)
    }
}