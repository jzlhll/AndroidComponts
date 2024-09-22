package com.au.module_android.fontutil

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import com.au.module.android.R
import com.au.module_android.widget.FontMode
import com.au.module_android.widget.TextViewCheckMode

/**
 * 全局字体默认文件。可以自行更换任意一项，目前虽然一样。
 */

const val FONT_NORMAL_PATH = "fonts/SourceHanSansSCNormal.otf"
const val FONT_MEDIUM_PATH = "fonts/SourceHanSansSCNormal.otf"
const val FONT_BOLD_PATH = "fonts/SourceHanSansSCNormal.otf"

const val FONT_NUMBER_PATH = "fonts/SourceHanSansSCNormal.otf"
const val FONT_NUMBER_MEDIUM_PATH = "fonts/SourceHanSansSCNormal.otf"
const val FONT_NUMBER_BOLD_PATH = "fonts/SourceHanSansSCNormal.otf"

private val fontFaceMap by lazy { hashMapOf<String, Typeface>() }

fun getOrCreateFontFace(context: Context, assetsPath: String?) : Typeface? {
    if(assetsPath.isNullOrEmpty()) return null
    val cacheTypeFace = fontFaceMap[assetsPath]
    if (cacheTypeFace != null) {
        return cacheTypeFace
    }
    val newTypeFace = Typeface.createFromAsset(context.assets, assetsPath)
    fontFaceMap[assetsPath] = newTypeFace
    return newTypeFace
}

/**
 * 设置assets里面的字体文件
 */
private fun TextView.setFontFromAssets(context: Context, assetsPath: String?) {
    if (isInEditMode) return
    val tf = getOrCreateFontFace(context, assetsPath) ?: return
    //性能优化，防止多次赋值
    if (tf == typeface) {
        return
    }

    typeface = tf
}

/**
 * 检查是否设置字体的粗体。
 * 这里支持，bold，mid，普通三种。查看attr.xml fontMode的定义和isNumberFont的定义。
 * 这样的话，就可以给FontEdit，FontText设置三种模式的字体。
 *
 * xml中给FontEdit，FontText添加fontMode、isNumberFont来实现粗体或者数字类型字体。
 * 代码中通过修改代码来实现。
 */
fun TextView.checkBoldAndSetFont(cxt: Context, attrs: AttributeSet?) : TextViewCheckMode {
    //具体的判断。优先使用。
    var isNumber = false
    var fontMode: FontMode = FontMode.NORMAL

    if (attrs != null) {
        val sa = cxt.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
        when (sa.getString(R.styleable.CustomTextView_fontMode)) {
            FontMode.BOLD.mode -> fontMode = FontMode.BOLD
            FontMode.MID.mode -> fontMode = FontMode.MID
            null -> {
                val textStyle = attrs.getAttributeIntValue(
                    "http://schemas.android.com/apk/res/android",
                    "textStyle",
                    -1
                )
                if (textStyle == 1) { //解析textStyle是否是bold。
                    fontMode = FontMode.BOLD
                } else if (textStyle == -1) {
                    //如果textStyle申明在控件上，是可以的。
                    //如果设置在@style/xxx中，是不支持的。这里采取名字匹配策略二次解决。
                    val sid = attrs.getAttributeResourceValue(null, "style", 0) //same as: attrs?.getStyleAttribute()
                    if (sid != 0) {
                        val styleName = cxt.resources.getResourceEntryName(sid)
                        if (styleName.contains("BText")) {
                            fontMode = FontMode.BOLD
                        } else if (styleName.contains("MText")) {
                            fontMode = FontMode.MID
                        }
                    }
                }
            }
        }

        isNumber = sa.getBoolean(R.styleable.CustomTextView_isFontNum, false)
        sa.recycle()
    }

    paint.isFakeBoldText = fontMode != FontMode.NORMAL
    setFontFromAsset(context, fontMode, isNumber)
    return TextViewCheckMode(fontMode, isNumber)
}

fun TextView.setFontFromAsset(context: Context, mode: FontMode, isNumber:Boolean) {
    //todo 根据是否存在这些字体，来决定比如mid=bold，或者num=非num。
    if (isNumber) {
        when (mode) {
            FontMode.NORMAL -> setFontFromAssets(context, FONT_NUMBER_PATH)
            FontMode.MID -> setFontFromAssets(context, FONT_NUMBER_MEDIUM_PATH)
            FontMode.BOLD -> setFontFromAssets(context, FONT_NUMBER_BOLD_PATH)
        }
    } else {
        when (mode) {
            FontMode.NORMAL -> setFontFromAssets(context, FONT_NORMAL_PATH)
            FontMode.MID -> setFontFromAssets(context, FONT_MEDIUM_PATH)
            FontMode.BOLD -> setFontFromAssets(context, FONT_BOLD_PATH)
        }
    }
}