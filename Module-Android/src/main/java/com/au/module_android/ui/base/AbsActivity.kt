package com.au.module_android.ui.base

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.BuildConfig
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.R
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.ui.fullPaddingEdgeToEdge
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logdNoFile

@Deprecated("基础框架的一环，请使用BindingActivity或者ViewActivity")
open class AbsActivity : AppCompatActivity(), IFullWindow, IAnim {
    protected open val isNotCacheFragment = true //不进行自动保存Fragment用于恢复。

    override val enterAnim: Int?
        get() = null

    override val exitAnim: Int?
        get() = null

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

    //记录Activity resources configuration的uiMode
    private var mCurrentUiMode : Int = Int.MIN_VALUE

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        ToutiaoScreenAdapter.attach(this)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enterAnim?.let { if(it != 0) overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, it, R.anim.activity_stay) }
            exitAnim?.let { if(it != 0) overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, it) }
        }

        mCurrentUiMode = resources.configuration.uiMode
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setEdge(view)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setEdge(null)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        setEdge(view)
    }

    /**
     * 进行全屏实现
     */
    open fun setEdge(contentView:View?) {
        fullPaddingEdgeToEdge(this, this.window, contentView ?: findViewById(android.R.id.content))
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        //处理安卓8.0报错
        //Only fullscreen activities can request orientation
        ignoreError { super.setRequestedOrientation(requestedOrientation) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //判断是否是输入法范围
        if (isAutoHideIme() && ev?.action == MotionEvent.ACTION_DOWN) {
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //这里的不准。系统是先走Application的onConfigChanged，然后走这里。
        // 那么，我们会先更新themedContext，以themedContext优先。
        // 如果我们不更新application的uiMode就会有问题，而它已经deprecated。

        if (BuildConfig.SUPPORT_DARKMODE) {
            val newUiMode = DarkModeAndLocalesConst.themedContext?.resources?.configuration?.uiMode ?: newConfig.uiMode //todo
            //dark mode
            //不论是系统切换，还是app设置中强制切换都会触发Activity configurationChange
            if (DarkModeAndLocalesConst.isDarkModeFollowSystem()) {
                //1. 如果是跟随系统，有切换了，判断重建
                if (mCurrentUiMode != newUiMode) {
                    mCurrentUiMode = newUiMode
                    logdNoFile { "onConfigurationChanged system in activity newUIMode $mCurrentUiMode " }
                    recreate()
                }
            } else {
                //2. 如果不跟系统，这里得到的newConfig是不准的，可能是系统触发而来(应该抛弃)，也可能是自己设置而来（接受）
                //这个应该被抛弃，以DarkModeUtil里面为准，即AppCompatDelegate.getDefaultNightMode()
                val appIsForceDark = DarkModeAndLocalesConst.isForceDark()
                val curIsDark = (mCurrentUiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                if (curIsDark != appIsForceDark) {
                    logdNoFile { "onConfigurationChanged force in activity curUiMode $mCurrentUiMode curIsDark=$curIsDark, app=$appIsForceDark recreate!" }
                    recreate()
                } else {
                    logdNoFile { "onConfigurationChanged force in activity curUiMode $mCurrentUiMode curIsDark=$curIsDark, app=$appIsForceDark do nothing!" }
                }
            }
        }
    }

    open fun isAutoHideIme() = false

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(DarkModeAndLocalesConst.activityAttachBaseContext(newBase))
    }

    override fun finish() {
        super.finish()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            exitAnim?.let { if(it != 0) overridePendingTransition(0, it) }
        }
    }
}