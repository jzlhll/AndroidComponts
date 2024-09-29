package com.au.module_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.logdNoFile
import java.util.Locale

@Keep
enum class DarkMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}

@SuppressLint("StaticFieldLeak")
object DarkModeAndLocalesConst {
    var supportLocaleFeature = false
    var supportDarkModeFeature = false
    var supportLanguage = listOf(Locale.US)

    var themedContext:Context? = null
        private set

    private val logTag = "DarkModeLocales"
    val data = Data()
    private val defaultLocales:Locale
        get() {
            return supportLanguage[0]
        }

    fun applicationOnConfigurationChanged(app:Application, newConfig: Configuration) {
        val isDarkModeFollow = isDarkModeFollowSystem()
        val isLocaleFollow = isLocalesFollowSystem(app)
        logdNoFile(tag = logTag) { "application onConfigurationChanged isDarkModeFollow:$isDarkModeFollow isLocaleFollow:$isLocaleFollow" }
        logdNoFile(tag = logTag) { "application onConfigurationChanged uiMode:${newConfig.uiMode} appUiMode:${app.resources.configuration.uiMode}" }
        logdNoFile(tag = logTag) { "application onConfigurationChanged locales:${newConfig.locales.get(0)} appLocales:${app.resources.configuration.locales.get(0)}" }
    }

    /**
     * activity | application都对接进来。
     * Application进来以后，会将保存的locale和darkMode设置好给到application
     *
     * 在调用之前，请初始好2个support变量。
     */
    fun attachBaseContext(newBase: Context?) : Context? {
        val cxt = newBase ?: return null
        if (!supportLocaleFeature && !supportDarkModeFeature) return newBase

        val uiMode = if(supportDarkModeFeature) darkMode2ConfigurationInt(data.spCurrentAppDarkMode(cxt)) else null
        val locale = if(supportLocaleFeature) {
            val language = data.spCurrentLocale(cxt)
            if (!language.second) { //跟随系统
                null
            } else {
                language.first
            }
        } else {
            null
        }
        logdNoFile(tag = logTag) { "attachBase Context --->newBase locales ${newBase.resources.configuration.locales.get(0)} uiMode ${newBase.resources.configuration.uiMode}" }
        if (uiMode == null && locale == null) {
            return newBase
        }
        return createConfigurationContext(cxt, locale, uiMode)
    }

    fun applicationAttachContext(base:Context?) : Context?{
        val createdContext = attachBaseContext(base)
        themedContext = createdContext //那么，不论如何都有一份themedContext了。
        return createdContext
    }

    fun applicationOnCreated(app:Application) {
        setToMode(data.spCurrentAppDarkMode(app))
    }

    /**
     * 是否设置成了强制单边 强制dark，不跟随系统
     */
    fun isForceDark() : Boolean {
        val m = AppCompatDelegate.getDefaultNightMode()
        return m == AppCompatDelegate.MODE_NIGHT_YES
    }

    /**
     * 是否设置成了强制单边，强制light，不跟随系统
     */
    fun isForceLight() : Boolean {
        val m = AppCompatDelegate.getDefaultNightMode()
        return m == AppCompatDelegate.MODE_NIGHT_NO
    }

    fun isDarkModeFollowSystem() = !(isForceLight() || isForceDark())

    fun isLocalesFollowSystem(cxt: Context) : Boolean{
        val d = data.spCurrentLocale(cxt)
        return !d.second
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

    /**
     * 从设置：切换语言，携带传递数据。
     * 跟随系统请传入newLocale = null
     */
    fun settingChangeLanguage(app: Application, newLocale: Locale?) {
        data.saveCurrentLocale(app, newLocale)
        val uiMode = darkMode2ConfigurationInt(data.spCurrentAppDarkMode(app))

        logdNoFile(tag = logTag) { "switchLanguage $newLocale---->createConfigurationContext" }
        themedContext = createConfigurationContext(app, newLocale, uiMode)
    }

    fun settingChangeDarkMode(app:Context, mode: DarkMode, saveToSp:Boolean = true) {
        if(saveToSp) data.saveAppDarkMode(Globals.app, mode)
        setToMode(mode)
        val locale = data.spCurrentLocale(app).first
        val uiMode = darkMode2ConfigurationInt(mode)
        logdNoFile(tag = logTag) { "changeDarkMode $mode -->createConfigurationContext" }
        themedContext = createConfigurationContext(app, locale, uiMode)
    }

    private fun createConfigurationContext(context: Context?, locale: Locale?, uiMode: Int?): Context? {
        context ?: return null
        val configuration = Configuration(Resources.getSystem().configuration) //一定要拷贝一份，避免污染getSystem

        if (locale != null) {
            configuration.setLocales(LocaleList(locale))
            configuration.setLayoutDirection(locale)
        }

        if (uiMode != null) {
            configuration.uiMode = uiMode
        }

        logdNoFile(tag = logTag) { "createConfigurationContext ${locale?.toAndroidResStr()} $uiMode" }
        return context.createConfigurationContext(configuration)
    }

    /**
     * 更新Application的Resource local，应用不重启的情况才调用，因为部分会用到application中的context
     * 切记不能走新api create ConfigurationContext，亲测
     * @param context context
     * @param newLanguage 传入了，就代表需要改变
     * @param newUiMode 传入了，就代表需要改变

//    fun updateApplicationLocale(context: Application, locale: Locale?, uiMode:Int?) {
//        if (locale == null && uiMode == null) return
//
//        val resources: Resources = context.resources
//        val configuration: Configuration = resources.configuration
//
//        if (locale != null) {
//            configuration.setLocales(LocaleList(locale))
//            configuration.setLayoutDirection(locale)
//        }
//
//        if (uiMode != null) {
//            configuration.uiMode = uiMode
//        }
//
//        resources.updateConfiguration(configuration, resources.displayMetrics)
//    }      */

    private fun setToMode(mode: DarkMode) {
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
            }

            DarkMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            DarkMode.FOLLOW_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun darkMode2ConfigurationInt(mode: DarkMode) : Int?{
        return when(mode) {
            DarkMode.DARK -> Configuration.UI_MODE_NIGHT_YES
            DarkMode.FOLLOW_SYSTEM -> null
            DarkMode.LIGHT -> Configuration.UI_MODE_NIGHT_NO
        }
    }

    class Data {
        private val XML_NAME = "ResConfiguration"
        private val KEY_CUR_LANGUAGE = "currentLanguage"
        private val KEY_DARK_MODE = "appDarkMode"

        private var curLanguageSp:String? = null
        /**
         * 返回值的第二个参数：false表示我们不需要处理多语言，跟随系统走。true则是自定义语言。
         */
        fun spCurrentLocale(context: Context) : Pair<Locale, Boolean> {
            val cur = curLanguageSp ?: context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CUR_LANGUAGE, "")
                .also { curLanguageSp = it }
            if (cur.isNullOrEmpty()) {
                return getSystemLocalElseCNLocale() to false
            }
            val foundCur = supportLanguage.find { it.toAndroidResStr() == cur }
            if (foundCur != null) {
                return foundCur to true
            }
            return getSystemLocalElseCNLocale() to false
        }

        /**
         * 切换跟随系统的时候，清空。
         */
        fun saveCurrentLocale(context: Context, locale: Locale?) {
            curLanguageSp = locale?.toAndroidResStr() ?: ""
            val edit = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE).edit()
            if (locale == null) {
                edit.remove(KEY_CUR_LANGUAGE).commit() //就是要立刻保存
            } else {
                edit.putString(KEY_CUR_LANGUAGE, locale.toAndroidResStr()).commit() //就是要立刻保存
            }
        }

        val systemLocal: Locale
            get() =
                Resources.getSystem().configuration.getLocales().get(0)

        private fun getSystemLocalElseCNLocale(): Locale {
            supportLanguage.forEach {
                if (it.toAndroidResStr() == systemLocal.toAndroidResStr())
                    return it
            }
            return defaultLocales
        }

        private var _spCurrentAppDarkMode: DarkMode? = null
        /**
         * 当前sp中保存的标记. boolean表示是否已经设置过了。
         */
        fun spCurrentAppDarkMode(cxt: Context) : DarkMode {
            val m = _spCurrentAppDarkMode
            if (m == null) {
                val mode = SharedPrefUtil.getInt(cxt, KEY_DARK_MODE, 0)
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
                }.also {
                    _spCurrentAppDarkMode = it
                }
            }

            return m
        }

        fun saveAppDarkMode(cxt: Context, mode: DarkMode) {
            /**
             * MODE_NIGHT_FOLLOW_SYSTEM 跟随系统设置
             * MODE_NIGHT_NO 关闭暗黑模式
             * MODE_NIGHT_YES 开启暗黑模式
             * MODE_NIGHT_AUTO_BATTERY 系统进入省电模式时，开启暗黑模式。不一定有用。
             * MODE_NIGHT_UNSPECIFIED 未指定，默认值
             */

            val edit = cxt.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE).edit()
            when (mode) {
                DarkMode.DARK -> {
                    edit.putInt(KEY_DARK_MODE, 2)
                }
                DarkMode.LIGHT -> {
                    edit.putInt(KEY_DARK_MODE, 1)
                }
                DarkMode.FOLLOW_SYSTEM -> {
                    edit.putInt(KEY_DARK_MODE, 0)
                }
            }
            edit.apply()
            _spCurrentAppDarkMode = mode
        }
    }
}

fun Locale.toAndroidResStr() : String {
    return language + "_" + country
}