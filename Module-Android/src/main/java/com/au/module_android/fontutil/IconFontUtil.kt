package com.au.module_android.fontutil

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import com.au.module_android.Globals

/**
iconFont使用
能够解决很多问题，对齐，居中，能兼容右侧空白等问题。
代码指南：
val linkedDeviceName = "$deviceName \ue602 \ue601"
val len = linkedDeviceName.length
val iconTf = getOrCreateFontFace(BaseGlobalConst.app, "fonts/iconfont.ttf")!!
val ss = SpannableStringBuilder(linkedDeviceName)
ss.setSpan(CustomTypefaceSpan("", iconTf), len - 3, len, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
val sizeSpan = RelativeSizeSpan(1.25f)
ss.setSpan(sizeSpan, len - 1, len, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
binding.deviceName.text = ss

使用指南：
www.iconfont.cn，
下载项目ttf得到iconfont.ttf。
getOrCreateFontFace得到TF。
unicode的由来是iconfont的16进制，比如 `&#xe602;` 改成`\ue602`
通过CustomTypefaceSpan, RelativeSizeSpan等修改语句中的unicode部分。

上传iconFont新图标的遇到的问题：
 figma，下载svg，导入一个自定义figma页面，
将所有图层修改outline stroke。
然后flatten，
再用插件fill rule editor
 再导出就得到一个改好的svg。
 可以上传iconFont。
 */

/**
 * 一部分一部分的拼接
 */
interface IIconFontPart

/**
 * 拼接文字
 */
data class IconFontNormalPart(val normalText:String) : IIconFontPart

/**
 * 拼接上一个unicode的iconFont
 */
open class IconFontIconPart(val unicode:Char, val colorStr:String? = null, val relativeSize:Float? = null) : IIconFontPart

/**
 * 要求TextView自身已经具有常规的字体；常规的textSize；常规的颜色。
 *
 * 再来设置结合iconFontPart。
 */
fun TextView.setIconFont(vararg parts: IIconFontPart) {
    val sb = StringBuilder()
    parts.forEach {
        if (it is IconFontNormalPart) {
            sb.append(it.normalText)
        } else if (it is IconFontIconPart) {
            sb.append(it.unicode)
        }
    }
    val text = sb.toString()
    val ss = SpannableStringBuilder(text)

    var len = 0
    val iconTf = getOrCreateFontFace(Globals.app, "fonts/iconfont.ttf")!!
    parts.forEach {
        if (it is IconFontNormalPart) {
            len += it.normalText.length
        } else if (it is IconFontIconPart) {
            ss.setSpan(CustomTypefaceSpan("", iconTf), len, len + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            if (it.colorStr != null) {
                ss.setSpan(ForegroundColorSpan(Color.parseColor(it.colorStr)), len, len + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (it.relativeSize != null) {
                ss.setSpan(RelativeSizeSpan(it.relativeSize), len, len + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            len += 1 //最后
        }
    }
    setText(ss)
}