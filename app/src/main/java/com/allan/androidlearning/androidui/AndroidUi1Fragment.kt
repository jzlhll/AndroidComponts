package com.allan.androidlearning.androidui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.allan.androidlearning.databinding.FragmentAndroidUi1Binding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

class AndroidUi1Fragment : BindingFragment<FragmentAndroidUi1Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        buttons()
        blocks()
        darkModeAndLocalesTest()
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
        binding.androidUiSwitchLayout.initValue(false, false)
        binding.androidUiSwitchBtnLayout.initValue(false)
    }

    @SuppressLint("DefaultLocale")
    private fun Color.toRgbStr() :String{
        val r = String.format("%.1f", red() * 255)
        val g = String.format("%.1f", green() * 255)
        val b = String.format("%.1f", blue() * 255)
        return "r $r,g $g,b $b"
    }

    fun darkModeAndLocalesTest() {
        val str1 = getStringCompat(R.string.string_test_locale)
        val str2 = Globals.getString(R.string.string_test_locale)
        val str3 = getString(R.string.string_test_locale)

        val color1 = getColor(com.tyiot.module.base.R.color.color_text_normal)
        val color2 = getColorCompat(com.tyiot.module.base.R.color.color_text_normal)

        val drawable1 = getDrawable(R.drawable.icon_servings)
        val drawable2 = getDrawableCompat(R.drawable.icon_servings)

        binding.text.textFromSpanBean("switchEnvironmentBindData",
            listOf(
                SpanBean(data.text + "\n", textColor = getColor(com.tyiot.module.base.R.color.color_text_normal), textSize = 15),
                SpanBean(data.hint, textColor = getColor(com.tyiot.module.base.R.color.color_text_desc), textSize = 13)
            )
        )
        binding.image1.setImageDrawable(data.drawable1)
        binding.image1.visibility = if(data.drawable1 == null) View.GONE else View.VISIBLE
        binding.image2.setImageDrawable(data.drawable2)
        binding.image2.visibility = if(data.drawable2 == null) View.GONE else View.VISIBLE

        data.color1?.let { mBinding.testColor1.setTextColor(it) }
        data.color2?.let { mBinding.testColor2.setTextColor(it) }
        binding.testColor1.visibility = if (data.color1 == null) View.GONE else View.VISIBLE
        binding.testColor2.visibility = if (data.color2 == null) View.GONE else View.VISIBLE
    }
}