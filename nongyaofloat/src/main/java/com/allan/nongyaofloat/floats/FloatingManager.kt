package com.allan.nongyaofloat.floats

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.au.module_android.utils.SingletonHolder

class FloatingManager private constructor(context: Context) {
 
    //获得WindowManager对象
    private var mWindowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
 
    companion object : SingletonHolder<FloatingManager, Context>(::FloatingManager)
 
 
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