package com.allan.androidlearning.androidui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentAndroidUi1Binding
import com.au.module_android.DarkModeAndLocalesConst
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
    private fun Color.toRgbStr() :String {
        val r = String.format("%d", (red() * 255).toInt())
        val g = String.format("%d", (green() * 255).toInt())
        val b = String.format("%d", (blue() * 255).toInt())
        return "($r, $g, $b)"
    }

    fun darkModeAndLocalesTest() {
        val str1 = getString(R.string.app_name)
        val str2 = Globals.app.getString(R.string.app_name, )
        val str3 = DarkModeAndLocalesConst.themedContext?.getString(R.string.app_name)

        val color1 = resources.getColor(com.au.module_androidcolor.R.color.color_text_normal)
        val color2 = Globals.app.getColor(com.au.module_androidcolor.R.color.color_text_normal)
        val color3 = DarkModeAndLocalesConst.themedContext!!.getColor(com.au.module_androidcolor.R.color.color_text_normal)

        val drawable1 = resources.getDrawable(R.drawable.test_night_image, null)
        val drawable2 = Globals.app.getDrawable(R.drawable.test_night_image)
        val drawable3 = DarkModeAndLocalesConst.themedContext?.getDrawable(R.drawable.test_night_image)

        binding.text.text = "$str1, $str2, $str3" + "\n" +
                            "${Color.valueOf(color1).toRgbStr()}, ${Color.valueOf(color2).toRgbStr()}, ${Color.valueOf(color3).toRgbStr()}"

        binding.image1.setImageDrawable(drawable1)
        binding.image2.setImageDrawable(drawable2)
        binding.image3.setImageDrawable(drawable3)

        binding.testColor1.setTextColor(color1)
        binding.testColor2.setTextColor(color2)
        binding.testColor3.setTextColor(color3)
    }
}