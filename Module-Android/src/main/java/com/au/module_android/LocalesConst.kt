package com.au.module_android

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import android.util.DisplayMetrics
import android.util.Log
import java.util.Locale

/**
 * @author allan
 * @date :2024/9/27 14:57
 * @description: 多语言切换工具
 */
object LocalesConst {
    private const val XML_NAME = "Language"
    private const val KEY_CUR_LANGUAGE = "currentLanguage"

    const val LANGUAGE_EN = "en_US"
    const val LANGUAGE_CN = "zh_CN"
    const val LANGUAGE_TW = "zh_TW"

    fun activityAttachBaseContext(newBase: Context?) : Context? {
        if (!BuildConfig.SUPPORT_LOCALES) {
            return newBase
        }
        val cxt = newBase ?: return null
        val language = getLanguageSP(cxt)
        if (!language.second) { //跟随系统：直接返回
            return newBase
        }
        return createConfigurationContext(cxt, language.first)
    }

    /**
     * 当Application的attachBaseContext回调时，调用它
     */
    fun applicationAttachBaseContext(base:Context?) : Context? {
        if (!BuildConfig.SUPPORT_LOCALES) {
            return base
        }

        base ?: return null
        val language = getLanguageSP(base)
        if (!language.second) { //跟随系统：直接返回
            return base
        }
        return createConfigurationContext(base, language.first)
    }

    /**
     * 当Application  onConfigurationChanged(newConfig: Configuration) 触发
     */
    fun applicationConfigurationChanged(application: Application, newConfig: Configuration) {
        //todo
        Log.d("au_log", "applicationConfigurationChanged " + newConfig.locales)
    }

    /**
     * 切换语言，携带传递数据
     * LANGUAGE_TW
     * LANGUAGE_EN
     * LANGUAGE_CN
     */
    fun switchLanguage(language: String) {
        setLanguageSP(Globals.app, language)

        if (language.isEmpty()) {
            val resources: Resources = Resources.getSystem()
            val configuration: Configuration = resources.configuration
            configuration.setLocales(configuration.locales)
            configuration.setLayoutDirection(configuration.locales.get(0))
            val dm: DisplayMetrics = resources.displayMetrics
            resources.updateConfiguration(configuration, dm)
        } else {
            val resources: Resources = Globals.app.resources
            val configuration: Configuration = resources.configuration
            val locale = supportLanguage[language]
            configuration.setLocales(LocaleList(locale))
            configuration.setLayoutDirection(locale)
            val dm: DisplayMetrics = resources.displayMetrics
            resources.updateConfiguration(configuration, dm)
        }

        DarkModeConst.onLocaleChanged(language)
    }

    private var curLanguageSp:String? = null

    /**
     * 返回值的第二个参数：false表示我们不需要处理多语言，跟随系统走。true则是自定义语言。
     */
    fun getLanguageSP(context: Context) : Pair<String, Boolean> {
        val cur = curLanguageSp ?: context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE)
                                   .getString(KEY_CUR_LANGUAGE, "")
                                   .also { curLanguageSp = it }
        if (cur.isNullOrEmpty()) {
            return getSystemLocalElseCN() to false
        }
        return cur to true
    }

    /**
     * 切换跟随系统的时候，清空。
     */
    private fun setLanguageSP(context: Context, language: String?) {
        curLanguageSp = language
        val edit = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE).edit()
        if (language.isNullOrEmpty()) {
            edit.remove(KEY_CUR_LANGUAGE).commit() //就是要立刻保存
        } else {
            edit.putString(KEY_CUR_LANGUAGE, language).commit() //就是要立刻保存
        }
    }

    val systemLocal: Locale
        get() =
            Resources.getSystem().configuration.getLocales().get(0)

    /**
     * 当前系统语言如果匹配我们支持的，就返回。如果我们不支持，则返回CN。
     */
    private fun getSystemLocalElseCN(): String {
        supportLanguage.forEach {
            if (it.value?.language == systemLocal.language)
                return it.key
        }
        return LANGUAGE_CN
    }

    /**
     * 默认支持的语言
     */
    private val supportLanguage: HashMap<String, Locale?> = object : HashMap<String, Locale?>(4) {
        init {
            put(LANGUAGE_EN, Locale.ENGLISH)
            put(LANGUAGE_CN, Locale.SIMPLIFIED_CHINESE)
            put(LANGUAGE_TW, Locale.TRADITIONAL_CHINESE)
            //Locale locale = new Locale("ug", Locale.CHINA.getCountry());
           // put(LANGUAGE_DE, Locale.GERMAN)
           // put(LANGUAGE_FR, Locale.FRENCH)
        }
    }

    private fun createConfigurationContext(context: Context, language: String): Context? {
        if (!supportLanguage.containsKey(language)) return context

        val resources = context.resources
        val configuration: Configuration = resources.configuration
        val locale = supportLanguage[language]
        Log.d("au_log", "current Language locale = $locale uiMode: ${configuration.uiMode}")
        val localeList = LocaleList(locale)
        configuration.setLocales(localeList)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}