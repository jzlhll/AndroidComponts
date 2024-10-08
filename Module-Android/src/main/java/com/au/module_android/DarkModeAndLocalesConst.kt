package com.au.module_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatDelegate
import com.au.module_android.utils.logdNoFile
import java.util.Locale

@IntDef(*[Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES])
@Retention(AnnotationRetention.SOURCE)
annotation class DarkMode

@SuppressLint("StaticFieldLeak")
object DarkModeAndLocalesConst {
    /**
     * 请按照Locale("zh", "CN")的方式创建Locale。不要使用Locale.US, Locale.CHINESE
     */
    var supportLocales = listOf(Locale("en", "US"))

    var themedContext:Context? = null
        private set

    private const val TAG = "DarkModeLocales"

    /**
     * activity | application都对接进来。
     * Application进来以后，会将保存的locale和darkMode设置好给到application
     *
     * 在调用之前，请初始好2个support变量。
     */
    fun activityAttachBaseContext(newBase: Context?, fromTag: String = "activity") : Context? {
        if (!BuildConfig.SUPPORT_LOCALES && !BuildConfig.SUPPORT_DARKMODE) return newBase
        val cxt = newBase ?: return null

        if (isDarkModeFollowSystem() && isLocalesFollowSystem(newBase)) {
            logdNoFile(tag = TAG) { "attachBase Context all null--->newBase locales ${newBase.resources.configuration.locales.get(0)} uiMode ${newBase.resources.configuration.uiMode}" }
            return newBase
        }
        return createConfigContext(cxt, isDarkModeFollowSystem(), isLocalesFollowSystem(newBase), fromTag)
    }

    fun appAttachBaseContext(base:Context?) : Context?{
        themedContext = activityAttachBaseContext(base, fromTag = "application")
        return themedContext
    }

    fun appOnCreated(app:Application) {
        setToMode(spCurrentAppDarkMode(app))
    }

    fun appOnConfigurationChanged(app:Application, newConfig: Configuration) {
        if (!BuildConfig.SUPPORT_LOCALES && !BuildConfig.SUPPORT_DARKMODE) return

        val isDarkModeFollow = isDarkModeFollowSystem()
        val isLocaleFollow = isLocalesFollowSystem(app)
        logdNoFile(tag = TAG) {
                "application onConfigurationChanged: " +
                "darkModeFollow:$isDarkModeFollow localeFollow:$isLocaleFollow " +
                "newUiMode:${newConfig.uiMode} appUiMode:${app.resources.configuration.uiMode} " +
                "newLocale:${newConfig.locales.get(0).toAndroidResStr()} appLocale:${app.resources.configuration.locales.get(0).toAndroidResStr()}" }

        logdNoFile(tag = TAG) { "application onConfigurationChanged: sys:" + systemLocal.toAndroidResStr() + " " + Resources.getSystem().configuration.uiMode }

        var hasChanged = false
        if (isDarkModeFollow) {
            hasChanged = true
        }
        if (isLocaleFollow) {
            hasChanged = true
        }

        if (hasChanged) {
            themedContext = createConfigContext(app, isDarkModeFollow, isLocaleFollow, "application onConfigurationChanged")
        }
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
        spCurrentLocale(cxt) ?: return true
        return false
    }

    /**
     * 获取当前是否是黑暗模式。true 就是黑暗模式。
     * warning：不得使用Application。
     *
     * 这个函数application初始化阶段调用不准。
     * 其他只要是任意activity范围onCreate和其他生命周期都可以。
     */
    fun detectDarkMode(cxt:Context) : Boolean {
        if (cxt is Application) {
            throw RuntimeException()
        }
        val mode = cxt.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 如果传入了就使用；否则就使用存储的。
     */
    private fun createConfigContext(context: Context?, uiModeFollowSys:Boolean, localFollowSys:Boolean, fromTag:String = "") : Context? {
        context ?: return null
        val configuration = Configuration(Resources.getSystem().configuration) //一定要拷贝一份，避免污染getSystem
        logdNoFile(tag = TAG) { "----$fromTag----create Config Context: " +
                "cloned: ${configuration.uiMode} ${configuration.locales.get(0).toAndroidResStr()} " +
                "sys: " + Resources.getSystem().configuration.uiMode + " " + Resources.getSystem().configuration.locales.get(0).toAndroidResStr()
        }

        if (!localFollowSys) {
            val locale = spCurrentLocale(context)
            if (locale != null) {
                configuration.setLocales(LocaleList(locale))
                configuration.setLayoutDirection(locale)
            }
        }

        if (!uiModeFollowSys) {
            val uiMode = spCurrentAppDarkMode(context)
            if (uiMode == Configuration.UI_MODE_NIGHT_YES || uiMode == Configuration.UI_MODE_NIGHT_NO) {
                val filter = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
                configuration.uiMode = uiMode or filter
            }
        }

        logdNoFile(tag = TAG) { "---created Config Context: new is: ${configuration.uiMode} ${configuration.locales.get(0).toAndroidResStr()}" }
        return context.createConfigurationContext(configuration)
    }

    fun settingChangeDarkMode(app:Application, @DarkMode uiMode: Int?, saveToSp:Boolean = true) {
        if(saveToSp) saveAppDarkMode(Globals.app, uiMode)
        setToMode(uiMode)
        logdNoFile(tag = TAG) { "setting ChangeDarkMode $uiMode -->$uiMode ?:(${Resources.getSystem().configuration.uiMode})" }

        themedContext = createConfigContext(app, isDarkModeFollowSystem(), isLocalesFollowSystem(app))
    }

    /**
     * 从设置：切换语言，携带传递数据。
     * 跟随系统请传入newLocale = null
     */
    fun settingChangeLanguage(app: Application, newLocale: Locale?) {
        saveCurrentLocale(app, newLocale)
        logdNoFile(tag = TAG) { "setting ChangeLanguage----> ${newLocale?.toAndroidResStr()} ?:(${Resources.getSystem().configuration.locales})" }
        themedContext = createConfigContext(app, isDarkModeFollowSystem(), isLocalesFollowSystem(app))
    }

    private fun setToMode(@DarkMode mode: Int?) {
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private const val XML_NAME = "ResConfiguration"
    private const val KEY_CUR_LANGUAGE = "currentLanguage"
    private const val KEY_DARK_MODE = "appDarkMode"

    private var curLanguageAndroidStr:String? = null
    /**
     * 返回null就是跟随系统
     */
    fun spCurrentLocale(context: Context) : Locale? {
        val cur = curLanguageAndroidStr ?: context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUR_LANGUAGE, "")
            .also { curLanguageAndroidStr = it }
        if (cur.isNullOrEmpty()) {
            return null
        }
        return supportLocales.find { it.toAndroidResStr() == cur } //这里肯定是找到了。因为我们保存的就是我们设定的。
    }

    /**
     * 切换跟随系统的时候，清空。
     */
    private fun saveCurrentLocale(context: Context, locale: Locale?) {
        curLanguageAndroidStr = locale?.toAndroidResStr() ?: ""
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

    private var _spCurrentAppDarkMode: Int? = null

    @DarkMode
    fun spCurrentAppDarkMode(cxt: Context) : Int? {
        val m = _spCurrentAppDarkMode
        if (m == null) {
            val sp = cxt.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE)
            val mode = sp.getInt(KEY_DARK_MODE, -999)
            _spCurrentAppDarkMode = mode
            return mode
        }

        if (m == -999) {
            return null
        }
        return m
    }

    private fun saveAppDarkMode(cxt: Context, @DarkMode mode: Int?) {
        val edit = cxt.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE).edit()
        if (mode == null) {
            edit.remove(KEY_DARK_MODE).commit()
        } else {
            edit.putInt(KEY_DARK_MODE, mode).commit()
        }
        _spCurrentAppDarkMode = mode
    }
}

fun Locale.toAndroidResStr() : String {
    return language + "_" + country
}