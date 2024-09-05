package com.au.module_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatDelegate
import com.au.module_android.sp.SharedPrefUtil

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@IntDef(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES, Configuration.UI_MODE_NIGHT_UNDEFINED)
annotation class DarkModeInt

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

    fun changeDarkMode(@DarkModeInt mode:Int, saveSp:Boolean = true) {
        /**
         * MODE_NIGHT_FOLLOW_SYSTEM 跟随系统设置
         * MODE_NIGHT_NO 关闭暗黑模式
         * MODE_NIGHT_YES 开启暗黑模式
         * MODE_NIGHT_AUTO_BATTERY 系统进入省电模式时，开启暗黑模式。不一定有用。
         * MODE_NIGHT_UNSPECIFIED 未指定，默认值
         */
        when (mode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 1)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 0)
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                if(saveSp) SharedPrefUtil.putInt(Globals.app, "app_dark_mode", 2)
            }
        }

        themedContext = createContext(mode) //重建
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
    @DarkModeInt
    private fun Context.spCurrentAppDarkMode() : Int {
        val mode = SharedPrefUtil.getInt(this, "app_dark_mode", 0)
        return when (mode) {
            0 -> {
                Configuration.UI_MODE_NIGHT_UNDEFINED
            }
            1 -> {
                Configuration.UI_MODE_NIGHT_NO
            }
            else -> {
                Configuration.UI_MODE_NIGHT_YES
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