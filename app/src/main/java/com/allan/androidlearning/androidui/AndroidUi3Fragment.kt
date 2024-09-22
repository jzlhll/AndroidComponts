package com.allan.androidlearning.androidui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityManager
import com.allan.androidlearning.databinding.FragmentAndroidUi3Binding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.fontutil.FONT_NUMBER_PATH
import com.au.module_android.fontutil.getOrCreateFontFace
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.base.findDialogByContentFragment
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.logd
import com.au.module_android.widget.ViewStubPro
import com.au.module_androidui.widget.NumberPickerCompat
import com.au.module_androidui.widget.SimpleNumberPicker
import com.au.module_androidui.widget.SimpleNumberPickerCompat
import kotlin.math.max

class AndroidUi3Fragment : BindingFragment<FragmentAndroidUi3Binding>() {

    //default time
    private val time = 7200
    private val maxSeconds = if(Math.random() > 0.5) 15 * 3600 else 3600
    private val minSeconds = 60

    private var hourPickerWrap : NumberPickerCompat? = null
    private lateinit var minPickerWrap : NumberPickerCompat
    private var mWhenHourMaxMinuteMax = 59
    private var mHasHourMaxHour = 0
    private var isPostingReset = false
    private val resetMinutePickerTo0Runnable = Runnable {
        minPickerWrap.setValue(0)
        isPostingReset = false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.gotoEditBtn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEditFragment::class.java)
        }
        binding.gotoEdit2Btn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEdit2Fragment::class.java)
        }
        binding.gotoEdit3Btn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEdit3Fragment::class.java)
        }

        val timeHour = time / 3600
        val timeMinute = (time % 3600) / 60
        val hour = maxSeconds / 3600 + if(maxSeconds % 3600 > 0) 1 else 0

        mHasHourMaxHour = hour
        mWhenHourMaxMinuteMax = (maxSeconds % 3600) / 60

        if (maxSeconds > 3600) {
            launchNumberPickerWrap(binding.hourPicker, true)
            launchNumberPickerWrap(binding.minutePicker, false)

            initNumberPicker(hourPickerWrap!!, hour, 0, timeHour, true)
            initNumberPicker(minPickerWrap, 59, 0, timeMinute, true)

            if (!isAccessibilityEnable()) {
                //做点偏移
                val dp = 8f.dp
                (hourPickerWrap as SimpleNumberPickerCompat).numberPicker.setDrawXOffsetDp(dp)
                (minPickerWrap as SimpleNumberPickerCompat).numberPicker.setDrawXOffsetDp(-dp)
                binding.hourUnit.post {
                    binding.hourUnit.translationX = dp
                }
                binding.minuteUnit.post {
                    binding.minuteUnit.translationX = -dp
                }
            }
        } else {
            launchNumberPickerWrap(binding.minutePicker, false)

            initNumberPicker(minPickerWrap, maxSeconds / 60, max(1, minSeconds / 60), time / 60, true)
        }

        if (maxSeconds > 3600) {
            hourPickerWrap?.setOnValueChangedListener(object : NumberPickerCompat.OnValueChangeListener {
                override fun onValueChange(oldVal: Int, newVal: Int) {
                    if (newVal == mHasHourMaxHour) {
                        minPickerWrap.setValue(0)
                    }
                }
            })

            minPickerWrap.setOnValueChangedListener(object : NumberPickerCompat.OnValueChangeListener {
                override fun onValueChange(oldVal: Int, newVal: Int) {
                    if (mHasHourMaxHour > 0 && hourPickerWrap?.getValue() == mHasHourMaxHour) {
                        isPostingReset = true
                        binding.root.removeCallbacks(resetMinutePickerTo0Runnable)
                        binding.root.postDelayed(resetMinutePickerTo0Runnable, 350)
                    }
                }
            })

        } else {
            hideHour()
        }

        binding.seekBarHost.setOnTouchListener(View.OnTouchListener {_, e->
            binding.seekBar.onTouchEvent(e)
        })
    }


    fun answer(): Int {
        val hourPicker = hourPickerWrap ?: return minPickerWrap.getValue() * 60

        return hourPicker.getValue() * 3600 + minPickerWrap.getValue() * 60
    }

    fun hideHour() {
        binding.hourUnit.gone()
        binding.hourPicker.gone()
        binding.pickerSpace.gone()
//        binding.pickCenterSpace.gone()
    }

    fun initNumberPicker(picker: NumberPickerCompat, max: Int, min: Int, def: Int, changeToDef: Boolean) {
        logd { "allan initNumberPickerNew" }
        picker.setMaxValue(max)
        picker.setMinValue(min)
        if(changeToDef) picker.setValue(def)
        getOrCreateFontFace(Globals.app, FONT_NUMBER_PATH)?.let { picker.setTypeFace(it) }
        picker.setTextColor(requireContext().getColor(com.au.module_androidcolor.R.color.color_text_normal))
        picker.setSecondTextColor(requireContext().getColor(com.au.module_androidcolor.R.color.color_text_desc))
        picker.setTextSize(20f.dp) //文字大小，这里最好使用px转dp

        if (picker is SimpleNumberPickerCompat) {
            picker.setSelectionDividerHeight(0) //两道分割线的高度
        }

        picker.setWrapSelectorWheel(true)
        //做一些格式转换
        picker.setFormatter { value->
            if (value < 10) {
                "0$value"
            } else {
                "" + value
            }
        }
    }

    private fun launchNumberPickerWrap(stub: ViewStubPro, isHour:Boolean) {
        val clazz = if (!isAccessibilityEnable()) SimpleNumberPicker::class.java else android.widget.NumberPicker::class.java
        stub.setReplaceViewClass(clazz)
        val numberPicker = stub.launch()
        val wrap = NumberPickerCompat.create(numberPicker)
        if (isHour) {
            hourPickerWrap = wrap
        } else {
            minPickerWrap = wrap
        }
    }

    fun isAccessibilityEnable() : Boolean {
        val manager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE).asOrNull<AccessibilityManager>()
        return manager?.isTouchExplorationEnabled ?: false
    }
}