package com.allan.androidlearning.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import com.google.android.material.textfield.TextInputEditText

/**
 * @author allan.jiang
 * Date: 2022/12/15
 * Description TODO
 */
class MyEditText : TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        showSoftInputOnFocus = false
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        showSoftInputOnFocus = false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event!!.hasNoModifiers()) {
            if (!isEnabled) {
                return true
            }
            if (isClickable && isPressed) {
                isPressed = false
                if (!mHasPerformedLongPress) {
                    // This is a tap, so remove the longpress check
                    removeLongPressCallback()
                    if (!event.isCanceled) {
                        return performClickInternal()
                    }
                }
            }
            return false
        }
        return super.onKeyUp(keyCode, event)
    }
}