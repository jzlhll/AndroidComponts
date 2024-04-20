package com.allan.autoclickfloat.activities.autooneclick

import android.widget.TextView
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.datastore.AppDataStore
import com.au.module_android.utils.gone

class SetupClickFloatView : BaseFloatingView(R.layout.view_floating_step) {
    var callback:(x:Int, y:Int)->Unit = {_,_->}

    init {
        mRoot.findViewById<TextView>(R.id.stepIndexTv)?.gone()

        touchUpCallback = {x,y->
            AppDataStore.save(
                "auto_continuous_click_point_x" to x,
                "auto_continuous_click_point_y" to y)
            callback(x, y)
        }
    }

    suspend fun showInit() {
        val x:Int = AppDataStore.read("auto_continuous_click_point_x", 0)
        val y:Int = AppDataStore.read("auto_continuous_click_point_y", 0)
        show(x, y)
    }
}