package com.au.module_android.ui.base

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.ignoreError

@Deprecated("基础框架的一环，请使用BindingActivity或者ViewActivity")
open class AbsActivity : AppCompatActivity() {
    companion object {
        const val KEY_INTENT_AUTO_HIDE_IME = "intent_auto_hide_ime"
    }

    protected open val isNotCacheFragment = true //不进行自动保存Fragment用于恢复。

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

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        ToutiaoScreenAdapter.attach(this)
        super.onCreate(savedInstanceState)
        isAutoHideIme = intent.getBooleanExtra(KEY_INTENT_AUTO_HIDE_IME, false)
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
                hideImeNew(window, focusView)
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

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if(isNotCacheFragment) removeCachedFragments(outState)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(isNotCacheFragment) removeCachedFragments(outState)
    }

    private fun removeCachedFragments(outState: Bundle) {
        //清空保存Fragment的状态数据
        if (false) { //非androidx
            outState.putParcelable("android:support:fragments", null)
            outState.putParcelable("android:fragments", null)
        } else { //androidx
            outState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key")?.let {
                it.remove("android:support:fragments")
                it.remove("android:fragments")
            }
        }
    }

}