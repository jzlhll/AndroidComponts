package com.allan.autoclickfloat.activities.autooneclick

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.Globals
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy

class SetupClickFloatView private constructor(): BaseFloatingView(R.layout.view_floating_step) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance:SetupClickFloatView? = null
        fun getInstance() : SetupClickFloatView {
            if (instance == null) {
                instance = SetupClickFloatView()
            }
            return instance!!
        }

        fun getInstanceOrNull() = instance
    }

    var callback:(x:Int, y:Int)->Unit = {_,_->}

    val workingColor = ColorStateList.valueOf(Globals.getColor(R.color.floating_working_color))
    val settingColor = ColorStateList.valueOf(Globals.getColor(R.color.floating_setting_color))

    val icon: ImageView

    private val clickAnim by unsafeLazy {
        ValueAnimator
            .ofFloat(0f, 0.1f, 0.15f, 1f)
            .apply {
                duration = 120
                doOnStart {
                    mRoot.alpha = 0f
                }
                addUpdateListener {
                    val alpha = it.animatedFraction * mNotAlpha
                    Log.d(Const.TAG, "alpha $alpha")
                    mRoot.alpha = alpha
                }
                doOnEnd {
                    mRoot.alpha = mNotAlpha
                }

                interpolator = LinearInterpolator()
            }
    }

    private fun startClickAnim() {
        clickAnim.start()
    }

    private fun stopClickAnim() {
        clickAnim.cancel()
    }

    init {
        mRoot.findViewById<TextView>(R.id.stepIndexTv)?.gone()
        icon = mRoot.findViewById(R.id.ivIcon)
        touchUpCallback = {x, y->
            Log.d(Const.TAG, "autoClick saved x=$x, y=$y")
            val location = IntArray(2)
            mRoot.getLocationOnScreen(location)
            Log.d(Const.TAG, "screen location ${location[0]} * ${location[1]}")
            Const.autoOnePoint.saveAutoOnePoint(x, y, location[0], location[1], Const.rotationLiveData.value!!)
            callback(x, y)
        }
        Const.autoOnePoint.autoOnePointBeClickedData.observeForeverUnStick {
            stopClickAnim()
            startClickAnim()
        }
    }
}