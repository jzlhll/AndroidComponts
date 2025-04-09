package com.allan.autoclickfloat.activities.autofs

import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.lifecycleScope
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewAddBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.fontutil.FONT_NUMBER_PATH
import com.au.module_android.fontutil.getOrCreateFontFace
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.base.findDialog
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.widget.ViewStubPro
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import com.au.module_androidui.toast.ToastBuilder
import com.au.module_androidui.widget.NumberPickerCompat
import com.au.module_androidui.widget.SimpleNumberPicker
import com.au.module_androidui.widget.SimpleNumberPickerCompat
import kotlinx.coroutines.launch
import java.util.Calendar

class AutoStartAlarmDialog : BindingFragment<FragmentAutoStartupNewAddBinding>() {
    companion object {
        fun show(f: AutoStartAlarmFragment) {
            FragmentBottomSheetDialog.show<AutoStartAlarmDialog>(f.childFragmentManager, Bundle().apply {
                putBoolean("isEditMode", false)
            })
        }

        fun edit(ac:FragmentShellActivity, autoFsId:String) {
            FragmentBottomSheetDialog.show<AutoStartAlarmDialog>(ac.supportFragmentManager, Bundle().apply {
                putString("editAutoFsId", autoFsId)
                putBoolean("isEditMode", true)
            })
        }
    }

    private var plusDay = 0
    private var mEditAutoFsId:String? = null

    private fun initPlusDay() {
        binding.plusADayBtn.onClick {
            plusDay++
            binding.plusDayText.text = plusDay.toString()
        }
        binding.minusADayBtn.onClick {
            plusDay--
            if (plusDay < 0) {
                plusDay = 0
            }
            binding.plusDayText.text = plusDay.toString()
        }
    }

    private fun initSwitchBtn() {
        binding.switchBtn.initValue(isLeft = true, false)

        binding.switchBtn.valueCallback = { isLeft->
            changeEnablePlusDayControls(isLeft)
        }
    }

    private fun changeEnablePlusDayControls(isLeft: Boolean) {
        if (isLeft) {
            binding.plusADayBtn.isEnabled = true
            binding.minusADayBtn.isEnabled = true
            binding.plusDayText.setTextColor(Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal))
        } else {
            binding.plusADayBtn.isEnabled = false
            binding.minusADayBtn.isEnabled = false
            binding.plusDayText.setTextColor(Globals.getColor(com.au.module_androidcolor.R.color.color_text_desc))
        }
    }

    fun initNumberPicker(picker: NumberPickerCompat, max: Int, min: Int, def: Int, changeToDef: Boolean) {
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

    private lateinit var hourPickerWrap : NumberPickerCompat
    private lateinit var minPickerWrap : NumberPickerCompat

    fun isAccessibilityEnable() : Boolean {
        val manager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE).asOrNull<AccessibilityManager>()
        return manager?.isTouchExplorationEnabled ?: false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        launchNumberPickerWrap(binding.hourPicker, true)
        launchNumberPickerWrap(binding.minutePicker, false)
        val c = Calendar.getInstance()
        initNumberPicker(hourPickerWrap, 23, 0, c.get(Calendar.HOUR_OF_DAY), true)
        initNumberPicker(minPickerWrap, 59, 0, c.get(Calendar.MINUTE), true)
        initPlusDay()
        initSwitchBtn()
        val isEditMode = arguments?.getBoolean("isEditMode") == true
        if (isEditMode) {
            binding.titleTv.text = "编辑闹钟"
        }

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

        binding.addBtn.onClick {
            val oldId = mEditAutoFsId
            val isLoop = !binding.switchBtn.isLeft
            val dayPlus = if(isLoop) 0 else plusDay
            val isGood = if (oldId.isNullOrEmpty()) {
                AutoFsObj.addAlarmUiAndCheckStart(requireContext(),
                    hourPickerWrap.getValue(),
                    minPickerWrap.getValue(),
                    dayPlus,
                    isLoop)
            } else {
                AutoFsObj.editAlarmUiAndCheckStart(requireContext(),
                    hourPickerWrap.getValue(),
                    minPickerWrap.getValue(),
                    dayPlus,
                    isLoop,
                    oldId)
            }

            val d = findDialog(this)
            logd { "d $d" }
            if (!isGood) {
                ToastBuilder().setOnFragmentDialog(this).setMessage("时间已经过期，请重新选择或者加天。").toast()
                return@onClick
            }
            findDialog(this)?.dismissAllowingStateLoss()
        }
        binding.cancelBtn.onClick {
            findDialog(this)?.dismissAllowingStateLoss()
        }

        lifecycleScope.launch {
            initEdit()
        }
    }

    private fun initEdit() {
        val editAutoFsId = arguments?.getString("editAutoFsId")
        arguments?.remove("editAutoFsId")

        if (!editAutoFsId.isNullOrEmpty()) {
            val targetTs = AutoFsObj.targetTsListData.value?.find { it.autoFsId == editAutoFsId }
            if (targetTs != null) {
                mEditAutoFsId = editAutoFsId
                val ts = targetTs.targetTs
                val c = Calendar.getInstance()
                c.timeInMillis = ts
                val h = c.get(Calendar.HOUR_OF_DAY)
                val m = c.get(Calendar.MINUTE)
                hourPickerWrap.setValue(h)
                minPickerWrap.setValue(m)

                val c2 = TimeUtil.hourMinuteToCalendar(h, m, 0)
                val deltaTsBySecond = (ts - c2.timeInMillis + 10) / 1000 //发现总是会有点偏差。进行纠正。
                if (deltaTsBySecond > 0) {
                    val plusDay = deltaTsBySecond / (24 * 60 * 60)
                    this.plusDay = plusDay.toInt()
                    binding.plusDayText.text = "" + plusDay
                }

                val isLoop = targetTs.isLoop
                binding.switchBtn.setValue(!isLoop)
                changeEnablePlusDayControls(!isLoop)
            }
        }
    }
}