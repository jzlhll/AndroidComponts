package com.au.jobstudy

import android.os.Bundle
import com.au.jobstudy.databinding.FragmentMainMineBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

class MainMineFragment : BindingFragment<FragmentMainMineBinding>() {
    private var clickDebugCount = 0

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainMineBinding) {
        val appName = getString(R.string.app_name)
        val system = AndroidSdkMapping().currentVersionStr
        val name = "$appName${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}\n$system"
        viewBinding.logoText.text = name
        viewBinding.settingButton.setOnClickListener {
            if (clickDebugCount++ % 30 == 20) {

            }
        }
    }
}