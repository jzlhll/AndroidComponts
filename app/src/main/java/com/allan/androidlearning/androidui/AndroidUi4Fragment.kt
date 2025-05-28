package com.allan.androidlearning.androidui

import android.os.Bundle
import androidx.core.graphics.toColorInt
import com.allan.androidlearning.databinding.FragmentAndroidUi4Binding
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_androidui.widget.HorizontalScale3View

class AndroidUi4Fragment : BindingFragment<FragmentAndroidUi4Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        //设置初始化值
        binding.sizeView1.setRange(40, 102)
        binding.sizeView2.apply {
            setRange(33, 102, 35)
            setTextColor("#6D6D73".toColorInt())
            setIndicatorColor("#FF5252".toColorInt())
            setLineColor("#D9D9D9".toColorInt())
            setOnValueChangeListener(object : HorizontalScale3View.OnValueChangeListener {
                override fun onValueChanged(value: Int, action:String) {
                    binding.scaleText.text = "当前值：$value, $action"
                }

            })
        }
    }
}