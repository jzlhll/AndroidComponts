package com.au.jobstudy.views

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.text.CustomFontText

/**
 * @author allan.jiang
 * @date :2023/12/1 17:59
 * @description:
 */
class CountDownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CustomFontText(context, attrs) {
    private var mCurrentCountDown = 0

    private val run = Runnable {
        runBlock()
    }

    private fun runBlock() {
        text = "$mCurrentCountDown"
        mCurrentCountDown--
        if(mCurrentCountDown > 0) handler.postDelayed(run, 1000)
    }

    fun startCountDown(start:Int) {
        mCurrentCountDown = start
        runBlock()
        handler?.removeCallbacksAndMessages(null)
        handler?.postDelayed(run, 1000)
    }

    fun abortCountDown() {
        handler?.removeCallbacksAndMessages(null)
    }
}