package com.allan.autoclickfloat.activities.autofs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat.getSystemService
import com.allan.autoclickfloat.databinding.FragmentAutoStartupNewBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.fontutil.FONT_NUMBER_PATH
import com.au.module_android.fontutil.getOrCreateFontFace
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd
import com.au.module_android.widget.ViewStubPro
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
    private fun showTime(calendar:Calendar) {
        // 获取年、月、日、时、分、秒
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1 // 月份从 0 开始，需要 +1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val second: Int = calendar.get(Calendar.SECOND)
        // 格式化输出
        val dateTime = String.format(
            "%04d-%02d-%02d %02d:%02d:%02d",  // 格式：YYYY-MM-DD HH:MM:SS
            year, month, day, hour, minute, second
        )
        logd { "allan-alarm dateTime: $dateTime" }
    }

    //default time
    private lateinit var hourPickerWrap : NumberPickerCompat
    private lateinit var minPickerWrap : NumberPickerCompat

    fun isAccessibilityEnable() : Boolean {
        val manager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE).asOrNull<AccessibilityManager>()
        return manager?.isTouchExplorationEnabled ?: false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        launchNumberPickerWrap(binding.hourPicker, true)
        launchNumberPickerWrap(binding.minutePicker, false)

        val calendar = Calendar.getInstance()
        initNumberPicker(hourPickerWrap, 23, 0, calendar.get(Calendar.HOUR_OF_DAY), true)
        initNumberPicker(minPickerWrap, 59, 0, calendar.get(Calendar.MINUTE), true)

        binding.stopTimerBtn.tag = false

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
//
//        hourPickerWrap?.setOnValueChangedListener(object : NumberPickerCompat.OnValueChangeListener {
//            override fun onValueChange(oldVal: Int, newVal: Int) {
//            }
//        })
//
//        minPickerWrap.setOnValueChangedListener(object : NumberPickerCompat.OnValueChangeListener {
//            override fun onValueChange(oldVal: Int, newVal: Int) {
//            }
//        })

        binding.startTimerBtn.onClick(1000) {
            startAlarm()
        }
        binding.stopTimerBtn.onClick(1000) {
            cancelAlarm()
        }

        val oldPendingIntent = generatePendingIntent(requireContext(), true)
        if (oldPendingIntent == null) {
            logd { "allan-alarm old pending Intent is null" }
        } else {
            logd { "allan-alarm old pending is not null" }
        }
    }

    private val REQUEST_CODE = 118

    private fun generatePendingIntent(context: Context, isOld:Boolean) : PendingIntent? {
        val intent: Intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction("com.autoStartFs.ACTION_ALARM_TRIGGERED")
        val flag = if(isOld)
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE 表示不创建新实例
                    else
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  // 必须与设置时一致
            intent,
            flag
        )
    }

//    private fun nextAlarm() {
//        val alarmManager = getSystemService<T>(Context.ALARM_SERVICE) as AlarmManager?
//        val alarmClockInfo = alarmManager!!.nextAlarmClock
//
//        if (alarmClockInfo != null) {
//            val triggerTime = alarmClockInfo.triggerTime // 获取触发时间
//            val showIntent = alarmClockInfo.showIntent // 获取显示 Intent
//            Log.d("NextAlarm", "Trigger Time: " + Date(triggerTime).toString())
//        } else {
//            Log.d("NextAlarm", "No upcoming alarm")
//        }
//    }

    private fun cancelAlarm() {
        logd { "allan-alarm cancel it..." }
        val context = requireContext()
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val it = generatePendingIntent(context, true)
        if(it != null) alarmMgr.cancel(it)
    }

    private fun startAlarm() {
        val context = requireContext()
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Set the alarm to start at 8:30 a.m.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = hourPickerWrap.getValue()
        calendar[Calendar.MINUTE] = minPickerWrap.getValue()
        calendar[Calendar.SECOND] = 0
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        showTime(calendar)

        try {
            val it = generatePendingIntent(context, false)
            if (it != null) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, it)
                logd { "allanAlarm start alarm" }
            }
        } catch (e:SecurityException) {
            e.printStackTrace()
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
}