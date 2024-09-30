package com.allan.androidlearning.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.allan.androidlearning.EntroActivity
import com.allan.androidlearning.Locales
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentDarkModeSettingBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.DarkMode
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.toAndroidResStr
import com.au.module_android.ui.ToolbarManager
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.HtmlPart
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.useSimpleHtmlText
import com.au.module_android.utils.visible
import java.util.Locale

@EntroFrgName(priority = 10, customName = "通用设置")
class DarkModeAndLocalesFragment : BindingFragment<FragmentDarkModeSettingBinding>() {
    private val localePart = LocalesPart()

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        setTitle(R.string.settings)
        DarkModePart().initDarkMode()
        localePart.initLocales()
    }

    override fun hasToolbarManager(): ToolbarManager.MenuBean {
        return ToolbarManager.MenuBean(com.au.module_android.R.menu.right_menu_save, false) {
            if (it.itemId == com.au.module_android.R.id.menuOk) {
                localePart.afterChange()
            }
        }
    }

    private inner class LocalesPart {
        private val strMap by unsafeLazy { mapOf(
            Locales.LOCALE_JIANTI_CN to getString(R.string.language_cn),
            Locales.LOCALE_FANTI_CN to getString(R.string.language_tw),
            Locales.LOCALE_US to getString(R.string.language_en))  }

        private var enterLocale:Locale? = null
        private var tempLocale:Locale? = null

        fun changeUi(isFollowSystem:Boolean, curLocale:Locale?, isChanged:Boolean = true) {
            tempLocale = curLocale
            if (isFollowSystem) {
                binding.localesFollowSystemCheck.visible()
                binding.localesCNCheck.invisible()
                binding.localesTWCheck.invisible()
                binding.localesENCheck.invisible()
            } else {
                binding.localesFollowSystemCheck.invisible()
                val localesResStr = curLocale!!.toAndroidResStr()
                if (localesResStr == Locales.LOCALE_JIANTI_CN.toAndroidResStr()) {
                    binding.localesCNCheck.visible()
                } else {
                    binding.localesCNCheck.invisible()
                }
                if (localesResStr == Locales.LOCALE_US.toAndroidResStr()) {
                    binding.localesENCheck.visible()
                } else {
                    binding.localesENCheck.invisible()
                }
                if (localesResStr == Locales.LOCALE_FANTI_CN.toAndroidResStr()) {
                    binding.localesTWCheck.visible()
                } else {
                    binding.localesTWCheck.invisible()
                }
            }

            toolbarManager?.hideMenu()
            if (isChanged && tempLocale != enterLocale) {
                toolbarManager?.showMenu()
            }
        }

        fun initLocales() {
            val pair = DarkModeAndLocalesConst.data.spCurrentLocale(requireContext()) ?: return //later: 如果能进来就肯定已经设置好了。
            val isFollowSystem = !pair.second
            enterLocale = if(!isFollowSystem) pair.first else null
            changeUi(isFollowSystem, enterLocale, false)

            val systemLocal = DarkModeAndLocalesConst.data.systemLocal

            binding.followSystemTitle.useSimpleHtmlText(
                HtmlPart(getString(R.string.follow_system) + " "),
                HtmlPart("(" + (strMap[systemLocal] ?: systemLocal.displayName) + ")", "#999999"))
            binding.localesFollowSystemCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(true, null)
            }
            binding.localesCNCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(false, Locales.LOCALE_JIANTI_CN)
            }
            binding.localesTWCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(false, Locales.LOCALE_FANTI_CN)
            }
            binding.localesENCheck.parent.asOrNull<ViewGroup>()?.onClick {
                changeUi(false, Locales.LOCALE_US)
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

            Globals.app.startActivityFix(Intent(Globals.app, EntroActivity::class.java))
        }
    }

    private inner class DarkModePart {
        //进入本界面加载的sp信息

        fun initDarkMode() {
            DarkModeAndLocalesConst.data.spCurrentAppDarkMode(Globals.app).let {
                when (it) {
                    DarkMode.DARK -> {
                        setAsDarkMode(true)
                    }
                    DarkMode.LIGHT -> {
                        setAsLightMode(true)
                    }
                    DarkMode.FOLLOW_SYSTEM -> {
                        setAsAutomatic(true)
                    }
                }
            }

            binding.lightHost.onClick {
                setAsLightMode(false)
                DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, DarkMode.LIGHT)
            }

            binding.darkHost.onClick {
                setAsDarkMode(false)
                DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, DarkMode.DARK)
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
                    DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, if(curDark) DarkMode.DARK else DarkMode.LIGHT)
                } else {
                    setAsAutomatic(false)
                    DarkModeAndLocalesConst.settingChangeDarkMode(Globals.app, DarkMode.FOLLOW_SYSTEM)
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