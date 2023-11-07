package com.allan.androidlearning.utils

import android.content.Context

/**
 * author: allan
 * Time: 2022/11/25
 * Desc:
 */
fun callDensity(context:Context):Float {
    return context.resources.displayMetrics.density
//    get() = (if (screenAdaptationByWidth)
//        min(
//            appDisplayMetrics.widthPixels.toFloat(),
//            appDisplayMetrics.heightPixels.toFloat()
//        )
//    else
//        max(
//            appDisplayMetrics.widthPixels.toFloat(),
//            appDisplayMetrics.heightPixels.toFloat()
//        )) / screenAdaptationUiDp

    //    var screenAdaptationUiDp = 360
}