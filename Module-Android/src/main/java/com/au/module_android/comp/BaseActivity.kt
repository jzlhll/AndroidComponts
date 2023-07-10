package com.au.module_android.comp

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.ignoreError

/**
 * @author allan.jiang
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class BaseActivity : AppCompatActivity(), ICommon{
    companion object {
        const val KEY_INTENT_AUTO_HIDE_IME = "intent_auto_hide_ime"
    }

    var isAutoHideIme:Boolean = false
        private set

    /**
     * 给出额外信息的空间1
     */
    var object1:Any? = null
    /**
     * 给出额外信息的空间2
     */
    var object2:Any? = null
    /**
     * 给出额外信息的空间3
     */
    var object3:Any? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        isAutoHideIme = intent.getBooleanExtra(KEY_INTENT_AUTO_HIDE_IME, false)
        setContentView(onCommonCreateView(layoutInflater, null, savedInstanceState))
        onCommonAfterCreateView(this, savedInstanceState, resources)
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        //处理安卓8.0报错
        //Only fullscreen activities can request orientation
        ignoreError { super.setRequestedOrientation(requestedOrientation) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //判断是否是输入法范围
        if (isAutoHideIme && ev?.action == MotionEvent.ACTION_DOWN) {
            val focusView = currentFocus
            if (focusView != null && isShouldHideInput(focusView, ev)) {
                hideImeNew(window, focusView);
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideInput(v: View, event: MotionEvent):Boolean {
        if (v is EditText) {
            val leftTop = intArrayOf(0, 0)
            v.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        return false
    }
}