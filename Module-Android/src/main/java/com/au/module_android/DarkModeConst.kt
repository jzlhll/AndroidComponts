package com.au.module_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.logd

@Keep
enum class DarkMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}

@SuppressLint("StaticFieldLeak")
object DarkModeConst {
    internal var isEnabled = false //是否开启

    /**
     *  application的context无法识别night的颜色和drawable。解决方案。
     *  参考而改动。
     *  https://stackoverflow.com/questions/58323212/contextcompat-getcolor-ignore-nightmode
     *
     * 大概率不是null。Application初始化的时候，会DarkModeUtil changeMode，进而过来。
     */
    internal var themedContext: Context? = null

    /**
     * 是否设置成了强制单边，不跟随系统
     */
    fun isForceDark() : Boolean {
        val m = AppCompatDelegate.getDefaultNightMode()
        return m == AppCompatDelegate.MODE_NIGHT_YES
    }

    /**
     * 是否设置成了强制单边，不跟随系统
     */
    fun isForceLight() : Boolean {
        val m = AppCompatDelegate.getDefaultNightMode()
        return m == AppCompatDelegate.MODE_NIGHT_NO
    }

    /**
     * 跟随系统走
     */
    fun isFollowSystem() = !(isForceLight() || isForceDark())

    private fun createContext(newUiMode: Int?) : Context {
        val uiMode = newUiMode ?: Globals.app.resources.configuration.uiMode

        val filter = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()

        val configuration = Configuration(Globals.app.resources.configuration)
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        val targetUiMode = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO or filter
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES or filter
            else -> uiMode
        }
        configuration.uiMode = targetUiMode
        configuration.setLocales(Globals.app.resources.configuration.locales)
        logd(canHasFileLog = false) { "createeContext uiMode=$uiMode targetUiMode $targetUiMode applicationUiMode=${Globals.app.resources.configuration.uiMode} " }
        return Globals.app.createConfigurationContext(configuration)
    }

    fun onConfigurationChanged(app:Application, uiMode:Int) {
        app.resources.configuration.uiMode = uiMode

        if (!isEnabled) {
            return
        }

        if (!isFollowSystem()) {
            logd(canHasFileLog = false) { "onConfigurationChanged uiMode not accept not follow system ${app.resources.configuration.uiMode}" }
            return
        }

        //如果系统有变化；并且当前不是强制情况下才处理;
        //new 0926：有问题。每次都重建好了。
        onUiModeChanged(uiMode)
    }

    /**
     * 可能是别的事件，所以需要自行判断uiMode是否发生变化。
     */
    fun onLocaleChanged(newLanguage :String) {
        if (!isEnabled) {
            return
        }
        logd(canHasFileLog = false) { "onLocaleChanged $newLanguage" }
        onUiModeChanged(null)
    }

    /**
     * 不仅仅要接受系统的Application的onConfigurationChanged
     * 还要在自己切换的时候重建
     */
    private fun onUiModeChanged(uiMode: Int?) {
        if (!isEnabled) {
            return
        }

        themedContext = createContext(uiMode)
    }

    /**
     * 当前sp中保存的标记. boolean表示是否已经设置过了。
     */
    fun Context.spCurrentAppDarkMode() : Pair<DarkMode, Boolean> {
        val isSet = SharedPrefUtil.containsKey(this, "app_dark_mode")
        val mode = SharedPrefUtil.getInt(this, "app_dark_mode", 0)
        return when (mode) {
            0 -> {
                DarkMode.FOLLOW_SYSTEM
            }
            1 -> {
                DarkMode.LIGHT
            }
            else -> {
                DarkMode.DARK
            }
        } to isSet
    }

    /**
     * 获取当前是否是黑暗模式。true 就是黑暗模式。
     * warning：不得使用Application。
     *
     * 这个函数application初始化阶段调用不准。
     * 其他只要是任意activity范围onCreate和其他生命周期都可以。
     */
    fun detectDarkMode(cxt:Context) : Boolean {
//            if (cxt is Application) {
//                throw RuntimeException()
//            }
        val mode = cxt.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun changeDarkMode(mode: DarkMode, saveSp:Boolean = true) {
        if (!isEnabled) {
            return
        }
        /**
         * MODE_NIGHT_FOLLOW_SYSTEM 跟随系统设置
         * MODE_NIGHT_NO 关闭暗黑模式
         * MODE_NIGHT_YES 开启暗黑模式
         * MODE_NIGHT_AUTO_BATTERY 系统进入省电模式时，开启暗黑模式。不一定有用。
         * MODE_NIGHT_UNSPECIFIED 未指定，默认值
         */
        when (mode) {
            DarkMode.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 2)
            }
            DarkMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 1)
            }
            DarkMode.FOLLOW_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 0)
            }
        }

        val uiMode = when(mode) {
            DarkMode.DARK-> Configuration.UI_MODE_NIGHT_YES
            DarkMode.FOLLOW_SYSTEM -> null
            DarkMode.LIGHT -> Configuration.UI_MODE_NIGHT_NO
        }

        logd(canHasFileLog = false) { "change Dark Mode mode $mode $uiMode" }
        onUiModeChanged(uiMode)
    }

    /**
     * 初始化app的darkMode
     */
    fun initAppDarkMode(app:Application) {
        isEnabled = true

        val savedModePair = app.spCurrentAppDarkMode()
        val savedMode = savedModePair.first
        changeDarkMode(savedMode, false)
    }
}