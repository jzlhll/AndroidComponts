package com.allan.autoclickfloat.activities.autofs

import android.content.Intent
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.utils.logd
import com.au.module_android.utils.startActivityFix
import com.au.module_androidui.widget.disableAndStyle
import com.au.module_androidui.widget.enableAndStyle

/**
 * @author allan
 * @date :2024/9/25 9:49
 * @description:
 * new this class in onCreate or 全局变量。
 */
class Permission(val f:Fragment) {
    private val context
        get() = f.requireContext()

    private val iAutoFs = f as IAutoStartPermission

    private var isInitSeekBar = false

    @Volatile
    private var mLastChangeBrightnessTs = 0L

    private var mNeedChangeBrightnessValue = -1
    private var changeBrightnessRun = Runnable{
        mLastChangeBrightnessTs = System.currentTimeMillis()
        setScreenBrightness(mNeedChangeBrightnessValue)
    }

    init {
        if (f !is IAutoStartPermission) {
            throw RuntimeException("autofs Permission: Please let your fragment implement IAutoStartPermission")
        }

        f.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                getCurrentScreenBrightness()
                isAutoScreenBrightness()
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                val seekBar = iAutoFs.seekBar()
                if (!isInitSeekBar) {
                    isInitSeekBar = true
                    seekBar.let {
                        it.min = 0
                        it.max = 255
                        it.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                mNeedChangeBrightnessValue = progress
                                val cur = System.currentTimeMillis()
                                Globals.backgroundHandler.removeCallbacks(changeBrightnessRun)
                                if (cur - mLastChangeBrightnessTs > 150) {
                                    changeBrightnessRun.run()
                                } else {
                                    Globals.backgroundHandler.post(changeBrightnessRun)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            }
                        })
                    }
                }

                seekBar.progress = getCurrentScreenBrightness()
                if (canWrite()) {
                    logd { "Can write setting. allan." }
                    seekBar.enableAndStyle()
                    iAutoFs.hideGotoWriteSettingButton()
                } else {
                    logd { "Cannot write setting. allan." }
                    seekBar.disableAndStyle()
                    iAutoFs.showGotoWriteSettingButton()
                }
            }
        })
    }

    fun canWrite() = Settings.System.canWrite(context)

    private fun setScreenBrightness(brightness: Int) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
    }

    /**
     * 获取当前屏幕亮度
     *
     * @return
     */
    private fun getCurrentScreenBrightness(): Int {
        try {
            val anInt = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            logd { "allan getCurrentScreenBrightness $anInt" }
            return anInt
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * SCREEN_BRIGHTNESS_MODE判断是否设置自动亮度
     * @return
     */
    private fun isAutoScreenBrightness(): Boolean {
        var ans = false
        try {
            val mode = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
            ans = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            logd { "allan isAutoScreenBrightness $ans" }
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    fun goToManageSetting() {
        context.startActivityFix(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
    }
}