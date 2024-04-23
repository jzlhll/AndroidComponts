package com.allan.autoclickfloat.activities.autooneclick

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.Globals
import com.au.module_android.utils.gone

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

    val workingColor = ColorStateList.valueOf(Globals.app.getColor(R.color.floating_working_color))
    val settingColor = ColorStateList.valueOf(Globals.app.getColor(R.color.floating_setting_color))

    val icon: ImageView

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
    }
}