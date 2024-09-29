package com.allan.androidlearning.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.allan.androidlearning.EntroActivity
import com.allan.androidlearning.databinding.FragmentDarkModeSettingBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.DarkMode
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.postToMainHandler
import com.au.module_android.toAndroidResStr
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.HtmlPart
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.useSimpleHtmlText
import com.au.module_android.utils.visible
import java.util.Locale

@EntroFrgName(priority = 10, customName = "通用设置")
class DarkModeAndLocalesFragment : BindingFragment<FragmentDarkModeSettingBinding>() {

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        setTitle("设置")
        DarkModePart().initDarkMode()
        LocalesPart().initLocales()
    }

    override fun hasToolbar(): Boolean {
        return true
    }

    private inner class LocalesPart {
        fun initLocales() {
            val pair = DarkModeAndLocalesConst.data.spCurrentLocale(requireContext())
            val isFollowSystem = !pair.second
            val locales = pair.first
            if (isFollowSystem) {
                binding.localesFollowSystemCheck.visible()
                binding.localesCNCheck.invisible()
                binding.localesTWCheck.invisible()
                binding.localesENCheck.invisible()
            } else {
                binding.localesFollowSystemCheck.invisible()
                val localesResStr = locales.toAndroidResStr()
                if (localesResStr == Locale.CHINA.toAndroidResStr()) {
                    binding.localesCNCheck.visible()
                } else {
                    binding.localesCNCheck.invisible()
                }
                if (localesResStr == Locale.US.toAndroidResStr()) {
                    binding.localesENCheck.visible()
                } else {
                    binding.localesENCheck.invisible()
                }
                if (localesResStr == Locale.TAIWAN.toAndroidResStr()) {
                    binding.localesTWCheck.visible()
                } else {
                    binding.localesTWCheck.invisible()
                }
            }
            binding.followSystemTitle.useSimpleHtmlText(HtmlPart("跟随系统 "), HtmlPart("(" + DarkModeAndLocalesConst.data.systemLocal.displayName + ")", "#999999"))
            binding.localesFollowSystemCheck.parent.asOrNull<ViewGroup>()?.onClick {
                DarkModeAndLocalesConst.settingChangeLanguage(Globals.app, null)
            }
            binding.localesCNCheck.parent.asOrNull<ViewGroup>()?.onClick {
                DarkModeAndLocalesConst.settingChangeLanguage(Globals.app, Locale.CHINA)
                afterChange()
            }
            binding.localesTWCheck.parent.asOrNull<ViewGroup>()?.onClick {
                DarkModeAndLocalesConst.settingChangeLanguage(Globals.app, Locale.TAIWAN)
                afterChange()
            }
            binding.localesENCheck.parent.asOrNull<ViewGroup>()?.onClick {
                DarkModeAndLocalesConst.settingChangeLanguage(Globals.app, Locale.US)
                afterChange()
            }
        }

        private fun afterChange() {
//            postToMainHandler {
//                System.exit(0)
//                Globals.app.startActivityFix(Intent(Globals.app, EntroActivity::class.java)) //todo 优化好看一点
//            }

            Globals.activityList.forEach {
                it.finish()
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