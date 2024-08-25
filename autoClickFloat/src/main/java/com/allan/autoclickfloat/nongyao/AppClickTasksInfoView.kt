package com.allan.autoclickfloat.nongyao

import android.annotation.SuppressLint
import android.widget.TextView
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.floats.views.BaseFloatingView

class AppClickTasksInfoView : BaseFloatingView( R.layout.app_click_tasks_info) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: AppClickTasksInfoView? = null
        fun getInstance() : AppClickTasksInfoView {
            if (instance == null) {
                instance = AppClickTasksInfoView()
            }
            return instance!!
        }

        fun getInstanceOrNull() = instance
    }

    private var infoTv :TextView? = null
    init {
        infoTv = mRoot.findViewById(R.id.infoTv)

        disableTouch = true
    }

    fun updateInfo(info:String) {
        infoTv?.text = info
    }
}