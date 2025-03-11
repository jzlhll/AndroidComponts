package com.allan.autoclickfloat.activities.autofs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.accessibility.AccessibilityManager
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewBinding
import com.allan.autoclickfloat.taks.LifeCycleCountDowner
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.fontutil.FONT_NUMBER_PATH
import com.au.module_android.fontutil.getOrCreateFontFace
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.widget.ViewStubPro
import com.au.module_androidui.toast.toastOnTop
import com.au.module_androidui.widget.NumberPickerCompat
import com.au.module_androidui.widget.SimpleNumberPicker
import com.au.module_androidui.widget.SimpleNumberPickerCompat
import java.util.Calendar


/**
 * @author allan
 * @date :2024/9/24 11:25
 * @description:
 */
class AutoStartAlarmFragment : BindingFragment<FragmentAutoStartupNewBinding>() {
    //default time
    private lateinit var hourPickerWrap : NumberPickerCompat
    private lateinit var minPickerWrap : NumberPickerCompat

    fun isAccessibilityEnable() : Boolean {
        val manager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE).asOrNull<AccessibilityManager>()
        return manager?.isTouchExplorationEnabled ?: false
    }

    private var plusDay = 0

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
        binding.switchBtn.initValue(isLeft = AutoFsObj.isSwitchOnce(), false)
        binding.switchBtn.valueCallback = {
            AutoFsObj.saveSwitchOnce(it)
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        launchNumberPickerWrap(binding.hourPicker, true)
        launchNumberPickerWrap(binding.minutePicker, false)
        val c = Calendar.getInstance()
        initNumberPicker(hourPickerWrap, 23, 0, c.get(Calendar.HOUR_OF_DAY), true)
        initNumberPicker(minPickerWrap, 59, 0, c.get(Calendar.MINUTE), true)
        initPlusDay()
        initSwitchBtn()

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

        binding.startTimerBtn.onClick() {
            AutoFsObj.cancelAlarm(requireContext(), null)

            val timeInfo = AutoFsObj.startAlarmWhenClick(requireContext(), hourPickerWrap.getValue(), minPickerWrap.getValue(), plusDay)
            if (timeInfo != null) {
                binding.currentAlarm.text = timeInfo.timeStr
                countDowner.start(timeInfo.targetTs)
            } else {
                toastOnTop("时间已经过去，请重新选择，或者加天。", icon = "warn")
            }
        }

        binding.stopTimerBtn.onClick() {
            AutoFsObj.cancelAlarm(requireContext(), null)
            binding.currentAlarm.text = ""
            countDowner.cancelAndTrigger("")
        }

        initAlarm()
    }

    private fun initAlarm() {
        val oldPendingIntent = AutoFsObj.fetchPendingIntent(requireContext())
        if (oldPendingIntent == null) {
            logd { "allan-alarm old pending Intent is null" }
            AutoFsObj.cancelAlarm(requireContext(), null)
        } else {
            logd { "allan-alarm old pending is not null" }
            val targetTs = AutoFsObj.targetTsData.value ?: -1L
            val isOnce = AutoFsObj.isSwitchOnce()
            val curTs = System.currentTimeMillis()
            if (targetTs == -1L) { //有pendingIntent但是没有保存数据？那么就可以用户清理了存储。我们也清理掉任务好了。
                AutoFsObj.cancelAlarm(requireContext(), oldPendingIntent)
            } else {
                if (isOnce) {
                    if (targetTs - COUNT_DOWN_OFFSET_HALF >= curTs) {
                        countDowner.start(targetTs)
                    }
                } else {
                    if (targetTs >= curTs) {
                        countDowner.start(targetTs)
                    } else {
                        val nextTs = TimeUtil.targetTsToNextDayCalendar(targetTs).timeInMillis
                        countDowner.start(nextTs)
                    }
                }
            }
        }

        AutoFsObj.targetTsData.observeUnStick(this) {
            if (it == -1L) {
                binding.currentAlarm.text = ""
                binding.currentAlarmDesc.text = ""
            } else {
                countDowner.start(it)
            }
        }
    }

    private val COUNT_DOWN_OFFSET = 60_000L
    private val COUNT_DOWN_OFFSET_HALF = 45_000L

    private val countDowner by unsafeLazy { LifeCycleCountDowner(this, COUNT_DOWN_OFFSET).apply {
        countDowningAction = {
            binding.currentAlarmDesc.text = it
        }
        endAction = {
            binding.currentAlarmDesc.text = ""
        }
    } }

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
}

class AlarmReceiver : BroadcastReceiver() {
    private fun doIt(context: Context) {
        FragmentRootActivity.start(context, AutoFsScreenOnFragment::class.java)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // 1. 获取WakeLock保持设备唤醒
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::AlarmWakeLock"
        )
        wakeLock.acquire(10 * 1000)
        try {
            // 2. 执行定时任务（例如启动服务、发送通知等）
            logd { "allan-alarm do it in onReceiver!!!" }
            doIt(context)
        } finally {
            wakeLock.release() //try不做释放
        }
    }
}