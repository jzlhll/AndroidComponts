package com.allan.autoclickfloat.floats.bean

import androidx.annotation.Keep

/**
 * @author allan
 * @date :2024/4/15 15:23
 * @description:
 */
@Keep
data class AutoClickInfo(val action:String,     //点击间隔
                         val mInterval:Long = -1L,
                         val mPointX:Float = -1f,//点击坐标xy
                         val mPointY:Float = -1f)

//打开悬浮窗
const val ACTION_SHOW = "action_show"
//自动点击事件 开启/关闭
const val ACTION_START = "action_start"
const val ACTION_STOP = "action_stop"
//关闭悬浮窗
const val ACTION_REMOVE = "action_close"
