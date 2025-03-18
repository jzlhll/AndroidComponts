package com.allan.autoclickfloat.activities.autofs

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings

class AirplaneHelp {
    fun hasPermission(context: Context) = canWrite(context)

    fun toggleAirplaneMode(context:Context)
    {
        val cr = context.contentResolver
        //检查当前飞行模式的状态
        val isAirplaneModeOn = Settings.Global.getInt (cr, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
        //切换飞行模式开关
        Settings.Global.putInt(cr, Settings.Global.AIRPLANE_MODE_ON, if(isAirplaneModeOn) 0 else 1)
        //通知系统飞行模式状态已更改
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        intent.putExtra("state", !isAirplaneModeOn)
        context.sendBroadcast(intent)
    }

    fun isAirplaneMode(context:Context) : Boolean{
        val cr = context.contentResolver
        return Settings.Global.getInt (cr, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }
}