package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentDarkModeSettingBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.DarkMode
import com.au.module_android.DarkModeConst
import com.au.module_android.DarkModeConst.spCurrentAppDarkMode
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

@EntroFrgName
class DarkModeFragment : BindingFragment<FragmentDarkModeSettingBinding>() {
    //进入本界面加载的sp信息
    val enterSavedPair by unsafeLazy { requireContext().spCurrentAppDarkMode() }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        setTitle("深色模式")

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

    override fun hasToolbar(): Boolean {
        return true
    }
}