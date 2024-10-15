package com.allan.androidlearning.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import com.allan.androidlearning.EntryActivity
import com.allan.androidlearning.Locales
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentDarkModeSettingBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.MenuBean
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.HtmlPart
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.useSimpleHtmlText
import com.au.module_android.utils.visible

@EntryFrgName(priority = 10, customName = "通用设置")
class DarkModeAndLocalesFragment : BindingFragment<FragmentDarkModeSettingBinding>() {
    private val localePart = LocalesPart()

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        DarkModePart().initDarkMode()
        localePart.initLocales()
    }

    override fun toolbarInfo(): ToolbarInfo {
        return ToolbarInfo(getString(R.string.settings), menuBean = MenuBean(com.au.module_android.R.menu.right_menu_save, false) {
            if (it.itemId == com.au.module_android.R.id.menuOk) {
                localePart.afterChange()
            }
        })
    }

    private inner class LocalesPart {
        private val strMap by unsafeLazy { mapOf(
            Locales.LOCALE_JIANTI_CN_KEY to getString(R.string.language_cn),
            Locales.LOCALE_FANTI_CN_KEY to getString(R.string.language_tw),
            Locales.LOCALE_US_KEY to getString(R.string.language_en))  }

        private var enterLocale:String? = null
        private var tempLocale:String? = null

        fun changeUi(curLocale:String?, isChanged:Boolean = true) {
            tempLocale = curLocale
            binding.localesFollowSystemCheck.invisible()
            binding.localesCNCheck.invisible()
            binding.localesTWCheck.invisible()
            binding.localesENCheck.invisible()

            if (curLocale.isNullOrEmpty()) {
                binding.localesFollowSystemCheck.visible()
            }
            if (curLocale == Locales.LOCALE_JIANTI_CN_KEY) {
                binding.localesCNCheck.visible()
            }
            if (curLocale == Locales.LOCALE_FANTI_CN_KEY) {
                binding.localesTWCheck.visible()
            }
            if (curLocale == Locales.LOCALE_US_KEY) {
                binding.localesENCheck.visible()
            }

            toolbarManager?.hideMenu()
            if (isChanged && tempLocale != enterLocale) {
                toolbarManager?.showMenu()
            }
        }

        fun initLocales() {
            val locale = DarkModeAndLocalesConst.spCurrentLocaleKey(requireContext())
            enterLocale = locale
            changeUi(enterLocale, false)

            val systemLocal = DarkModeAndLocalesConst.systemLocal
            val sysLocalStr = systemLocal.language + "_" + systemLocal.country

            binding.followSystemTitle.useSimpleHtmlText(
                HtmlPart(getString(R.string.follow_system) + " "),
                HtmlPart("(" + (strMap[sysLocalStr] ?: systemLocal.displayName) + ")", "#999999"))
            binding.localesFollowSystemCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(null)
            }
            binding.localesCNCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(Locales.LOCALE_JIANTI_CN_KEY)
            }
            binding.localesTWCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(Locales.LOCALE_FANTI_CN_KEY)
            }
            binding.localesENCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(Locales.LOCALE_US_KEY)
            }
        }

        fun afterChange() {
            if (tempLocale == enterLocale) {
                return
            }

            DarkModeAndLocalesConst.settingChangeLanguage(Globals.app, tempLocale)

            if (true) {
                Globals.activityList.forEach {
                    it.finish()
                }
            } else {
                System.exit(0)
            }

            Globals.app.startActivityFix(Intent(Globals.app, EntryActivity::class.java))
        }
    }

    private inner class DarkModePart {
        //进入本界面加载的sp信息
        fun initDarkMode() {
            DarkModeAndLocalesConst.spCurrentAppDarkMode(Globals.app).let {
                when (it) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        setAsDarkMode(true)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        setAsLightMode(true)
                    }
                    else -> {
                        setAsAutomatic(true)
                    }
                }
            }

            binding.lightHost.onClick {
                setAsLightMode(false)
                DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, Configuration.UI_MODE_NIGHT_NO)
            }

            binding.darkHost.onClick {
                setAsDarkMode(false)
                DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, Configuration.UI_MODE_NIGHT_YES)
            }

            binding.switchBtn.valueCallback = { isClosed->
                //由于默认是跟随系统。第一次关闭需要特殊处理，设置为当前系统的状态。
                if (isClosed) {
                    val curDark = DarkModeAndLocalesConst.detectDarkMode(requireContext())
                    if (curDark) {
                        setAsDarkMode(false)
                    } else {
                        setAsLightMode(false)
                    }
                    DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, if(curDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
                } else {
                    setAsAutomatic(false)
                    DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, null)
                }
            }
        }

        private fun setAsAutomatic(isInit:Boolean) {
            binding.hideLayout.gone()
            if(isInit) binding.switchBtn.initValue(false) else binding.switchBtn.setValue(false)
            val curDark = DarkModeAndLocalesConst.detectDarkMode(requireContext())
            if (curDark) {
                binding.darkModeCheck.visible()
                binding.lightModeCheck.invisible()
            } else {
                binding.darkModeCheck.invisible()
                binding.lightModeCheck.visible()
            }
        }

        private fun setAsDarkMode(isInit:Boolean) {
            binding.hideLayout.visible()
            if(isInit) binding.switchBtn.initValue(true) else binding.switchBtn.setValue(true)
            binding.darkModeCheck.visible()
            binding.lightModeCheck.invisible()
        }

        private fun setAsLightMode(isInit:Boolean) {
            binding.hideLayout.visible()
            if(isInit) binding.switchBtn.initValue(true) else binding.switchBtn.setValue(true)
            binding.darkModeCheck.invisible()
            binding.lightModeCheck.visible()
        }
    }
}