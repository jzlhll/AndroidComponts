package com.allan.autoclickfloat.activities.autofs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.lifecycleScope
import com.allan.autoclickfloat.databinding.FragmentAutoStartupBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.fontutil.FONT_NUMBER_PATH
import com.au.module_android.fontutil.getOrCreateFontFace
import com.au.module_android.postToMainHandler
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.startOutActivity
import com.au.module_android.utils.visible
import com.au.module_android.widget.ViewStubPro
import com.au.module_androidui.toast.ToastUtil
import com.au.module_androidui.widget.NumberPickerCompat
import com.au.module_androidui.widget.SimpleNumberPicker
import com.au.module_androidui.widget.SimpleNumberPickerCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author allan
 * @date :2024/9/24 11:25
 * @description:
 */
class AutoStartFragment : BindingFragment<FragmentAutoStartupBinding>(), ISeekHelp {
    private val permission = SeekHelp(this) //必须保留。

    override fun showGotoWriteSettingButton() {
        if(!isFaking) binding.gotoSettingWriteBtn.visible()
    }

    override fun hideGotoWriteSettingButton() {
        if(!isFaking) binding.gotoSettingWriteBtn.gone()
    }

    override fun seekBar() = binding.brightnessSeekbar

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm:ss")

    private var mCurrentTimeJob: Job? = null

    fun currentTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val currentTime: String = sdf.format(currentTimeMillis)
        return currentTime
    }

    //default time
    private lateinit var hourPickerWrap : NumberPickerCompat
    private lateinit var minPickerWrap : NumberPickerCompat

    fun isAccessibilityEnable() : Boolean {
        val manager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE).asOrNull<AccessibilityManager>()
        return manager?.isTouchExplorationEnabled ?: false
    }

    private var mFakeClickCount = 0
    private var isFaking = false

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        launchNumberPickerWrap(binding.hourPicker, true)
        launchNumberPickerWrap(binding.minutePicker, false)

        val calendar = Calendar.getInstance()
        initNumberPicker(hourPickerWrap, 23, 0, calendar.get(Calendar.HOUR_OF_DAY), true)
        initNumberPicker(minPickerWrap, 59, 0, calendar.get(Calendar.MINUTE), true)

        binding.stopTimerBtn.tag = false

        binding.gotoSettingWriteBtn.onClick {
            goToManageSetting(requireContext())
        }

        binding.fakeView.onClick {
            mFakeClickCount++
            if (mFakeClickCount == 12) {
                isFaking = true
                binding.pickersHost.invisible()
                binding.startTimerBtn.invisible()
                binding.gotoSettingWriteBtn.invisible()
                if(binding.stopTimerBtn.tag as Boolean) binding.stopTimerBtn.gone()
                binding.fakeText.visible()
            }
            if (mFakeClickCount == 20) {
                mFakeClickCount = 0
                isFaking = false
                binding.pickersHost.visible()
                if(!canWrite(requireContext())) binding.gotoSettingWriteBtn.visible()
                if(binding.stopTimerBtn.tag as Boolean) {
                    binding.stopTimerBtn.visible()
                    binding.startTimerBtn.gone()
                } else {
                    binding.stopTimerBtn.gone()
                    binding.startTimerBtn.visible()
                }
                binding.fakeText.gone()
            }
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
            val answer = answer()
            startFunc(answer, false)
        }
        binding.stopTimerBtn.onClick(1000) {
            stopFunc()
        }
    }

    private var mLastAnswer : Pair<Long, String>? = null

    private fun startFunc(answer:Pair<Long, String>, tryLast:Boolean) {
        mLastAnswer = answer
        targetTimeMillis = answer.first
        if (targetTimeMillis > System.currentTimeMillis()) {
            targetTimeStr = answer.second
            if (!isFaking) {
                binding.startTimerBtn.gone()
                binding.stopTimerBtn.visible()
            }

            binding.stopTimerBtn.tag = true

            Globals.mainHandler.removeCallbacks(doItRun)
            postToMainHandler(doItRun, targetTimeMillis - System.currentTimeMillis())
        } else {
            if(!tryLast) ToastUtil.toastOnTop("时间错误！")
            targetTimeMillis = -1
        }
    }

    private fun stopFunc() {
        if(!isFaking) {
            binding.stopTimerBtn.gone()
            binding.startTimerBtn.visible()
        }
        binding.stopTimerBtn.tag = false

        Globals.mainHandler.removeCallbacks(doItRun)
        targetTimeMillis = -1L
    }

    private var targetTimeMillis = -1L
    private var targetTimeStr = ""

    fun answer(): Pair<Long, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourPickerWrap.getValue())
        calendar.set(Calendar.MINUTE, minPickerWrap.getValue())
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis to sdf.format(calendar.time)
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

    private fun Long.secondToHMS(): String{
        var totalTime = this
        var hour = 0
        var minute = 0
        var second = 0
        if (3600 <= totalTime) {
            hour = (totalTime / 3600).toInt()
            totalTime -= 3600 * hour
        }
        if (60 <= totalTime) {
            minute = (totalTime / 60).toInt()
            totalTime -= 60 * minute
        }
        if (0 <= totalTime) {
            second = totalTime.toInt()
        }

        val secondStr = if(second < 10) "0$second" else "" + second
        val minStr = if(minute < 10) "0$minute" else "" + minute
        val hourStr = if(hour < 10) "0$hour" else "" + hour

        if (hour > 0) {
            return "${hourStr}h:${minStr}m:${secondStr}s"
        }
        if (minute > 0) {
            return "${minStr}m:${secondStr}s"
        }
        return "${secondStr}s"
    }

    override fun onResume() {
        super.onResume()

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mLastAnswer?.let { ans->
            startFunc(ans, true)
        }

        mCurrentTimeJob?.cancel()
        mCurrentTimeJob = lifecycleScope.launch {
            while (true) {
                delay(999)
                val left = (targetTimeMillis - System.currentTimeMillis()) / 1000
                if (left > 0) {
                    val leftTimeStr = left.secondToHMS()
                    binding.currentTimeTv.text =
                        if(isFaking) "当前时间：${currentTime()}\n剩余时间：$leftTimeStr"
                        else "当前时间：${currentTime()} 目标时间:$targetTimeStr\n剩余时间：$leftTimeStr"
                } else {
                    binding.currentTimeTv.text = "当前时间：${currentTime()}"
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mCurrentTimeJob?.cancel()
        stopFunc()
    }

    private val doItRun = Runnable {
        doIt()
    }

    private fun doIt() {
        val pm = requireActivity().packageManager
        val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
        if (intent != null) {
            ToastUtil.toastOnTop("Time is up launch apk in 3s...")
            lifecycleScope.launch {
                delay(3000)
                requireContext().startOutActivity(intent)
            }
        } else {
            ToastUtil.toastOnTop("Time is up! No target apk!")
        }
    }

    private var mCanBack = false
    private var mLastBackTs = 0L

    override val customBackAction: (() -> Boolean)
        get() = {
            mCanBack = true
            if (mLastBackTs < System.currentTimeMillis() - 5000) {
                mLastBackTs = System.currentTimeMillis()
                mCanBack = false
            }
            if (mCanBack || targetTimeMillis == -1L) {
                true
            } else {
                ToastUtil.toastOnTop("再次点击返回才能退出，且定时失效。")
                false
            }
        }
}