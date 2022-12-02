package com.allan.androidlearning.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView


class MyTextView : AppCompatTextView {
    constructor(context: Context) : super(context)

    //改动代码，init的不规范和没有必要的二次设置font
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        checkBoldAndSetFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        checkBoldAndSetFont(context, attrs)
    }

    fun checkBoldAndSetFont(cxt: Context, attrs: AttributeSet?) {
        val textStyle = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res/android",
            "textStyle", -1)
        //1. find textStyle mask
        if (textStyle != -1) {
            //2. find style mask
            val sid = attrs?.getAttributeResourceValue(null, "style", 0) //same as: attrs?.getStyleAttribute()
            if (sid != null) {
                //3. get style's name
                val styleName = cxt.resources.getResourceEntryName(sid) //
            }
        }

    }
}