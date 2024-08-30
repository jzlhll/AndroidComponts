package com.allan.androidlearning.androidui

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentAndroidUi1Binding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

class AndroidUi1Fragment : BindingFragment<FragmentAndroidUi1Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        buttons()
        blocks()
    }

    fun buttons() {
        //必须设置点击事件才有按压色
        binding.androidUi1Primary.onClick {
        }
        binding.androidUi1Secondary.onClick {
        }
    }

    fun blocks() {
        binding.androidUiSwitchBlock1.initValue(true)
        binding.androidUiSwitchBlock2.initValue(false)
        binding.androidUiSwitchLayout.initValue(false)
        binding.androidUiSwitchBtnLayout.initValue(false)
    }
}