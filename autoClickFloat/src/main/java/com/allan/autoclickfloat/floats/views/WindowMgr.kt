package com.allan.autoclickfloat.floats.views

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.au.module_android.Globals

/**
 * @author allan
 * @date :2024/4/15 16:26
 * @description:
 */
object WindowMgr {
    var floatingSetting: FloatingSettingView? = null
    var floatingStep: FloatingStepView? = null

    //获得WindowManager对象
    private var mWindowManager: WindowManager = Globals.app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    /**
     * 添加悬浮窗
     * @param view
     * @param params
     * @return
     */
    fun addView(view: View, params: WindowManager.LayoutParams): Boolean {
        try {
            mWindowManager.addView(view, params)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 移除悬浮窗
     *
     * @param view
     * @return
     */
    fun removeView(view: View): Boolean {
        try {
            mWindowManager.removeView(view)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 更新悬浮窗参数
     *
     * @param view
     * @param params
     * @return
     */
    fun updateView(view: View, params: WindowManager.LayoutParams): Boolean {
        try {
            mWindowManager.updateViewLayout(view, params)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}