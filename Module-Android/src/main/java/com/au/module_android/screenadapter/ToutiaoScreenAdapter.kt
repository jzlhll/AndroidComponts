package com.au.module_android.screenadapter

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ComponentCallbacks
import android.content.res.Configuration

/**
 * 头条适配方案

px = dp * density
density = dpi / 160
px = dp * (dpi / 160)

densityDpi    屏幕密度     比如：densityDpi为160，则表示每英寸屏幕中的像素点有160个，
density       逻辑密度     densityDpi/160，以160为参照物的比例，比如：280的屏幕，则是1.75
scaledDensity 字体比例     默认与density相同。而手机可以设置字体的大小比例，则会变化。


val display = resources.displayMetrics
Log.d("xxx", "density: " + display.density
              + " densityDpi: " + display.densityDpi
              + " scaledDensity: " + display.scaledDensity)

displayMetrics density: 1.75 densityDpi: 280 scaledDensity: 1.75
displayMetrics widthPixels: 1200 heightPixels: 1836
 */
object ToutiaoScreenAdapter {
    private const val DEFAULT_UE_SIZE = 390 //设计稿以360px为准

    private var systemDensity:Float = 1f //大小
    private var systemScaleDensity:Float = 1f//字体大小

    private var targetDensity:Float = 1f
    private var targetScaleDensity:Float = 1f
    private var targetDensityDpi:Int = 160

    private var ueExceptWidthPx = DEFAULT_UE_SIZE

    private lateinit var application:Application

    private var enable = false

    fun init(app:Application, expectWidthPx:Int=DEFAULT_UE_SIZE) {
        enable = true
        this.application = app
        this.ueExceptWidthPx = expectWidthPx

        refresh()

        app.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0) {
                    refresh()
                }
            }

            override fun onLowMemory() {
            }
        })
    }

    private fun refresh() {
        systemDensity = application.resources.displayMetrics.density
        systemScaleDensity = application.resources.displayMetrics.scaledDensity

        val appDisplayMetrics = application.resources.displayMetrics

        targetDensity = appDisplayMetrics.widthPixels / (ueExceptWidthPx.toFloat())
        targetScaleDensity = targetDensity * systemScaleDensity / systemDensity
        targetDensityDpi = (160 * targetDensity).toInt()

        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaleDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
    }

    fun attach(activity: Activity) {
        if(!enable)return
        val displayMetrics = activity.resources.displayMetrics
        displayMetrics.density = targetDensity
        displayMetrics.scaledDensity = targetScaleDensity
        displayMetrics.densityDpi = targetDensityDpi
    }

    fun attach(dialog: Dialog) {
        if(!enable)return
        val displayMetrics = dialog.context.resources.displayMetrics
        displayMetrics.density = targetDensity
        displayMetrics.scaledDensity = targetScaleDensity
        displayMetrics.densityDpi = targetDensityDpi
    }

    override fun toString(): String {
        return "ScreenAdapter(systemDensity=$systemDensity, systemScaleDensity=$systemScaleDensity, targetDensity=$targetDensity, targetScaleDensity=$targetScaleDensity, targetDensityDpi=$targetDensityDpi)"
    }
}