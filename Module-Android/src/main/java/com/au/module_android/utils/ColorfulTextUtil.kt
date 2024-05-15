package com.au.module_android.utils

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.isDigitsOnly

fun toHtml(html: String?): Spanned {
    return if (html == null) {
        // return an empty spannable if the html is null
        SpannableString("")
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
        // we are using this flag to give a consistent behaviour
        Html.fromHtml(html.replace("\n", "<br>"), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html.replace("\n", "<br>"))
    }
}

fun toLinkString(str: String): Spanned {
    val fixStr = str.replace("\n", "<br>")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
        // we are using this flag to give a consistent behaviour
        Html.fromHtml("<u>$fixStr</u>", Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml("<u>$fixStr</u>")
    }
}

/**
 * @param text 内容
 * @param color 颜色 类似#fff000
 * @param bold 是否粗体
 * @param link 是否有下划线
 */
data class HtmlPart(val text:String,
                    val color:String? = null,
                    val bold:Boolean=false,
                    val link:Boolean=false)

/**
 * 将一些常用的东西，封装一些常用且简单的代码。
 * 至于更复杂的组合，比如textSize，斜体，等，自行编写。
 */
fun TextView.useSimpleHtmlText(vararg items : HtmlPart) {
    val sb = StringBuilder("")
    items.forEach {
        val fmt =
            if (it.link) {
                if (it.bold) {
                    if (it.color != null) { //下划线&粗体&颜色
                        "<b><u><font color='#%s'>%s</font></u></b>"
                    } else { //下划线&粗体
                        "<b><u>%s</u></b>"
                    }
                } else {
                    if (it.color != null) {//下划线&颜色
                        "<u><font color='#%s'>%s</font></u>"
                    } else {//下划线
                        "<u>%s</u>"
                    }
                }
            } else {
                if (it.bold) {
                    if (it.color != null) {//粗体&颜色
                        "<b><font color='#%s'>%s</font></b>"
                    } else { //粗体
                        "<b>%s</b>"
                    }
                } else {
                    if (it.color != null) {//颜色
                        "<font color='#%s'>%s</font>"
                    } else { //nothing
                        "%s"
                    }
                }
            }

        val part = if (it.color != null) {
            val c = if (it.color.startsWith("#")) {
                it.color.substring(1)
            } else {
                it.color
            }
            String.format(fmt, c, it.text)
        } else {
            String.format(fmt, it.text)
        }
        sb.append(part)
    }
    text = toHtml(sb.toString())
}

private const val INT_SPLIT_CHAR = " "
private const val FRACTION_SPLIT_CHAR = "/"

/**
 * 字符串转分数
 */
fun fraction(paramValue: String?): Triple<Int?/*整数*/, Int?/*分子*/, Int?/*分母*/> {
    if (paramValue.isNullOrBlank()) {
        return Triple(null, null, null)
    }
    if (paramValue.isDigitsOnly()) {
        return Triple(paramValue.toInt(), null, null)
    }
    var intNumber: Int? = null
    var molecularNumber: Int? = null
    var denominatorNumber: Int? = null
    var fractionString = paramValue
    if (paramValue.contains(INT_SPLIT_CHAR)) {
        val split = paramValue.split(INT_SPLIT_CHAR)
        intNumber = ignoreError { split.first().toInt() }
        fractionString = split.last()
    }
    if (fractionString.contains(FRACTION_SPLIT_CHAR)) {
        val split = fractionString.split(FRACTION_SPLIT_CHAR)
        molecularNumber = ignoreError { split.first().toInt() }
        denominatorNumber = ignoreError { split.last().toInt() }
    }
    return Triple(intNumber, molecularNumber, denominatorNumber)
}

fun fractionToHtml(triple: Triple<Int?, Int?, Int?>): String {
    val fractionUnicode = fractionToUnicode(triple.second!!, triple.third!!)
    if (triple.first == null) {
        if (fractionUnicode != null) return fractionUnicode
        return String.format("<sup>%d</sup>&frasl;<sub>%d</sub>", triple.second, triple.third)
    }
    if (fractionUnicode != null) return triple.first.toString() + fractionUnicode
    return String.format("%d<sup>%d</sup>&frasl;<sub>%d</sub>", triple.first, triple.second, triple.third)
}

/** 分数转unicode。预测分支。
 */
fun fractionToUnicode(molecular:Int, denominator:Int):String? {
    if (molecular == 1) {
        if (denominator == 2) {
            return "\u00BD"
        }
        if (denominator == 3) {
            return "\u2153"
        }
        if (denominator == 4) {
            return "\u00BC"
        }
        if (denominator == 8) {
            return "\u215B"
        }
        if (denominator == 5) {
            return "\u2155"
        }
        if (denominator == 6) {
            return "\u2159"
        }
    } else if (molecular == 2) {
        if (denominator == 3) {
            return "\u2154"
        }
        if (denominator == 5) {
            return "\u2156"
        }
    } else if (molecular == 3) {
        if (denominator == 4) {
            return "\u00BE"
        }
        if (denominator == 8) {
            return "\u215C"
        }
        if (denominator == 5) {
            return "\u2157"
        }
    } else if (molecular == 4) {
        if (denominator == 5) {
            return "\u2158"
        }
    } else if (molecular == 5) {
        if (denominator == 8) {
            return "\u215D"
        }
        if (denominator == 6) {
            return "\u215A"
        }
    } else if (molecular == 7) {
        if (denominator == 8) {
            return "\u215E"
        }
    }
    return null
}