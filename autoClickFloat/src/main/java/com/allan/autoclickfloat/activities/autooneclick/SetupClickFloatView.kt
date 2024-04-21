package com.allan.autoclickfloat.activities.autooneclick

import android.content.res.ColorStateList
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.Globals
import com.au.module_android.utils.gone

const val key_auto_continuous_click = "key_auto_continuous_click"

class SetupClickFloatView : BaseFloatingView(key_auto_continuous_click, R.layout.view_floating_step) {
    var callback:(x:Int, y:Int)->Unit = {_,_->}

    val workingColor = ColorStateList.valueOf(Globals.app.getColor(R.color.floating_working_color))
    val settingColor = ColorStateList.valueOf(Globals.app.getColor(R.color.floating_setting_color))

    val icon: AppCompatImageView

    val halfSize : Int
        get() = (mRoot.width shr 1)

    init {
        mRoot.findViewById<TextView>(R.id.stepIndexTv)?.gone()
        icon = mRoot.findViewById(R.id.ivIcon)
        touchUpCallback = {x,y->
            Log.d(Const.TAG, "autoClick saved x=$x, y=$y")
            val location = IntArray(2)
            mRoot.getLocationOnScreen(location)
            Log.d(Const.TAG, "screen location ${location[0]} * ${location[1]}")
            Const.autoOnePoint.saveAutoOnePoint(x, y, location[0], location[1])
            callback(x, y)
        }

    }
}