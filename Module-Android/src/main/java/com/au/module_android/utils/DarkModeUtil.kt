package com.au.module_android.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil

@Keep
enum class DarkMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

class DarkModeUtil {

    companion object {
        /**
         *  application的context无法识别night的颜色和drawable。解决方案。
         *  参考而改动。
         *  https://stackoverflow.com/questions/58323212/contextcompat-getcolor-ignore-nightmode
         *
         * 大概率不是null。Application初始化的时候，会DarkModeUtil changeMode，进而过来。
         */
        @SuppressLint("StaticFieldLeak")
        internal var themedContext: Context? = null

        fun configurationUiModeToStr(context: Context) : String{
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (uiMode) {
                Configuration.UI_MODE_NIGHT_YES -> return "[System is Dark]"
                Configuration.UI_MODE_NIGHT_NO -> return "[System is Light]"
                Configuration.UI_MODE_NIGHT_UNDEFINED -> return "[System is Undefined]"
            }
            return "[system is $uiMode]"
        }

        fun appNightModeToStr() : String{
            val appMode = AppCompatDelegate.getDefaultNightMode()
            when (appMode) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> return "[App is Follow System]"
                AppCompatDelegate.MODE_NIGHT_YES -> return "[App is Dark]"
                AppCompatDelegate.MODE_NIGHT_NO -> return "[App is Light]"
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> return "[App is Undefined]"
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> return "[App is Auto battery]"
            }
            return "[App is $appMode]"
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
    }

    fun changeDarkMode(mode:DarkMode, saveSp:Boolean = true) {
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

        onUiModeChanged(cvtMode(mode))
    }

    /**
     * 不仅仅要接受系统的Application的onConfigurationChanged
     * 还要在自己切换的时候重建
     */
    private fun onUiModeChanged(newUiMode:Int) {
        themedContext = createContext(newUiMode)
    }

    /**
     * 将自己的mode转变为系统的uiMode
     */
    private fun cvtMode(mode:DarkMode) : Int {
        return when (mode) {
            DarkMode.DARK-> Configuration.UI_MODE_NIGHT_YES
            DarkMode.FOLLOW_SYSTEM -> Configuration.UI_MODE_TYPE_UNDEFINED
            DarkMode.LIGHT -> Configuration.UI_MODE_NIGHT_NO
        }
    }

    private fun createContext(uiMode:Int) : Context {
        val filter = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()

        val configuration = Configuration(Globals.app.resources.configuration)
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        configuration.uiMode = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> Configuration.UI_MODE_NIGHT_NO or filter
            AppCompatDelegate.MODE_NIGHT_YES -> Configuration.UI_MODE_NIGHT_YES or filter
            else -> uiMode
        }
        return Globals.app.createConfigurationContext(configuration)
    }

    /**
     * 当前sp中保存的标记
     */
    private fun Context.spCurrentAppDarkMode() : DarkMode {
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
        }
    }

    /**
     * 初始化app的darkMode
     */
    fun initAppDarkMode(app:Application) {
        val savedMode = app.spCurrentAppDarkMode()
        changeDarkMode(savedMode, false)
    }
}