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
import com.au.module_android.DarkModeConst
import com.au.module_android.DarkModeConst.spCurrentAppDarkMode
import com.au.module_android.Globals
import com.au.module_android.LocalesConst
import com.au.module_android.click.onClick
import com.au.module_android.postToMainHandler
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.HtmlPart
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.useSimpleHtmlText
import com.au.module_android.utils.visible
import kotlin.system.exitProcess

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
            val pair = LocalesConst.getLanguageSP(requireContext())
            val isFollowSystem = !pair.second
            val locales = pair.first
            if (isFollowSystem) {
                binding.localesFollowSystemCheck.visible()
                binding.localesCNCheck.invisible()
                binding.localesTWCheck.invisible()
                binding.localesENCheck.invisible()
            } else {
                binding.localesFollowSystemCheck.invisible()
                if (locales == LocalesConst.LANGUAGE_CN) {
                    binding.localesCNCheck.visible()
                } else {
                    binding.localesCNCheck.invisible()
                }
                if (locales == LocalesConst.LANGUAGE_EN) {
                    binding.localesENCheck.visible()
                } else {
                    binding.localesENCheck.invisible()
                }
                if (locales == LocalesConst.LANGUAGE_TW) {
                    binding.localesTWCheck.visible()
                } else {
                    binding.localesTWCheck.invisible()
                }
            }
            binding.followSystemTitle.useSimpleHtmlText(HtmlPart("跟随系统 "), HtmlPart("(" + LocalesConst.systemLocal.displayName + ")", "#999999"))
            binding.localesFollowSystemCheck.parent.asOrNull<ViewGroup>()?.onClick {
                LocalesConst.switchLanguage("")
            }
            binding.localesCNCheck.parent.asOrNull<ViewGroup>()?.onClick {
                LocalesConst.switchLanguage(LocalesConst.LANGUAGE_CN)
                afterChange()
            }
            binding.localesTWCheck.parent.asOrNull<ViewGroup>()?.onClick {
                LocalesConst.switchLanguage(LocalesConst.LANGUAGE_TW)
                afterChange()
            }
            binding.localesENCheck.parent.asOrNull<ViewGroup>()?.onClick {
                LocalesConst.switchLanguage(LocalesConst.LANGUAGE_EN)
                afterChange()
            }
        }

        private fun afterChange() {
            requireActivity().window.decorView.asOrNull<FrameLayout>()?.addView(ProgressBar(requireContext()).also {
                it.layoutParams = FrameLayout.LayoutParams(24.dp, 24.dp).also { it.gravity = Gravity.CENTER }
            })

            postToMainHandler {
                System.exit(0)
                Globals.app.startActivityFix(Intent(Globals.app, EntroActivity::class.java)) //todo 优化好看一点
            }
        }
    }

    private inner class DarkModePart {
        //进入本界面加载的sp信息
        val enterSavedPair by unsafeLazy { requireContext().spCurrentAppDarkMode() }

        fun initDarkMode() {
            enterSavedPair.let {
                when (it.first) {
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
                DarkModeConst.changeDarkMode(DarkMode.LIGHT)
            }

            binding.darkHost.onClick {
                setAsDarkMode(false)
                DarkModeConst.changeDarkMode(DarkMode.DARK)
            }

            binding.switchBtn.valueCallback = { isClosed->
                //由于默认是跟随系统。第一次关闭需要特殊处理，设置为当前系统的状态。
                if (isClosed) {
                    val curDark = DarkModeConst.detectDarkMode(requireContext())
                    if (curDark) {
                        setAsDarkMode(false)
                    } else {
                        setAsLightMode(false)
                    }
                    DarkModeConst.changeDarkMode(if(curDark) DarkMode.DARK else DarkMode.LIGHT)
                } else {
                    setAsAutomatic(false)
                    DarkModeConst.changeDarkMode(DarkMode.FOLLOW_SYSTEM)
                }
            }
        }

        private fun setAsAutomatic(isInit:Boolean) {
            binding.hideLayout.gone()
            if(isInit) binding.switchBtn.initValue(false) else binding.switchBtn.setValue(false)
            val curDark = DarkModeConst.detectDarkMode(requireContext())
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