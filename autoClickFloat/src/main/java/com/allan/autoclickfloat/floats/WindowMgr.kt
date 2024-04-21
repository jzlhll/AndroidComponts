package com.allan.autoclickfloat.floats

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.Globals
import java.util.concurrent.ConcurrentHashMap

/**
 * @author allan
 * @date :2024/4/15 16:26
 * @description:
 */
object WindowMgr {
    /**
     * key是tag后续用于找回它，用来标记value View。
     */
    private val viewsMap = ConcurrentHashMap<String, BaseFloatingView>()

    fun findFloatView(tag:String) = viewsMap[tag]

    //获得WindowManager对象
    private var mWindowManager: WindowManager = Globals.app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    /**
     * 添加悬浮窗
     * @param view
     * @param params
     * @return
     */
    fun addView(tag:String, view: BaseFloatingView, params: WindowManager.LayoutParams): Boolean {
        try {
            mWindowManager.addView(view.mRoot, params)
            viewsMap[tag] = view
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
    fun removeView(tag:String, view: BaseFloatingView): Boolean {
        try {
            mWindowManager.removeView(view.mRoot)
            viewsMap.remove(tag)
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