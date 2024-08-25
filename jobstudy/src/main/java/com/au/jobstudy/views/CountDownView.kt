package com.au.jobstudy.views

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.widget.CustomFontText

/**
 * @author au
 * @date :2023/12/1 17:59
 * @description:
 */
class CountDownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CustomFontText(context, attrs) {
    private var mCurrentCountDown = 0

    private var endWorkCallback:(()->Unit)? = null

    private val run = Runnable {
        runBlock()
    }

    private fun runBlock() {
        text = "$mCurrentCountDown"
        mCurrentCountDown--
        if (isAttachedToWindow) {
            if(mCurrentCountDown > 0) handler.postDelayed(run, 1000) else endWorkCallback?.invoke()
        }
    }

    fun startCountDown(start:Int, endWorkCallback:()->Unit) {
        this.endWorkCallback = endWorkCallback
        mCurrentCountDown = start
        runBlock()
        handler?.removeCallbacksAndMessages(null)
        handler?.postDelayed(run, 1000)
    }

    fun abortCountDown() {
        handler?.removeCallbacksAndMessages(null)
    }
}