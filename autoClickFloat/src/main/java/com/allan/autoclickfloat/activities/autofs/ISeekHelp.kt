package com.allan.autoclickfloat.activities.autofs

import android.widget.SeekBar

/**
 * @author allan
 * @date :2024/9/25 14:22
 * @description:
 */
interface ISeekHelp {
    fun seekBar():SeekBar?
    fun showGotoWriteSettingButton()
    fun hideGotoWriteSettingButton()
}