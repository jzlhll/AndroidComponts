package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.au.module_android.fontutil.setFontFromAsset
import com.au.module_android.utils.forEachChild
import com.google.android.material.appbar.MaterialToolbar

/**
 * @author allan
 * @date :2024/8/21 14:04
 * @description:
 */
class CustomToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialToolbar(context, attrs) {
    //默认使用中字体，作为标题
    var isBlod = false
    var isMid = true

    init {
        post {
            forEachChild {
                if (it is TextView) {
                    if (isBlod) {
                        //        paint.isFakeBoldText = true
                        it.setFontFromAsset(context, FontMode.BOLD, false)
                    } else if (isMid) {
                        it.setFontFromAsset(context, FontMode.MID, false)
                    } else {
                        it.setFontFromAsset(context, FontMode.NORMAL, false)
                    }
                }
            }
        }
    }
}