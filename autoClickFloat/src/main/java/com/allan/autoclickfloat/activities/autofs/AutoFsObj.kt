package com.allan.autoclickfloat.activities.autofs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import com.au.module_cached.AppDataStore
import java.util.Calendar

/**
 * @author allan
 * @date :2024/9/25 15:36
 * @description:
 */
object AutoFsObj {
    var targetTsData = NoStickLiveData<Long>()

    fun init() {
        val targetTs = AppDataStore.readBlocked("autoFsTargetTs", -1L)
        targetTsData.setValueSafe(targetTs)
    }

    fun isSwitchOnce() : Boolean{
        return AppDataStore.readBlocked("autoFSAlarmSwitchOnce", true)
    }

    fun saveSwitchOnce(isSwitchOnce:Boolean) {
        AppDataStore.save("autoFSAlarmSwitchOnce" to isSwitchOnce)
    }

    private val REQUEST_CODE = 118

    private fun generatePendingIntent(context: Context) : PendingIntent {
        val intent: Intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction("com.autoStartFs.ACTION_ALARM_TRIGGERED")
        val flag = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  // 必须与设置时一致
            intent,
            flag
        )
    }

    fun fetchPendingIntent(context: Context) : PendingIntent? {
        val intent: Intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction("com.autoStartFs.ACTION_ALARM_TRIGGERED")
        val flag = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  // 必须与设置时一致
            intent,
            flag
        )
    }

    fun cancelAlarm(context:Context, pit: PendingIntent?) {
        logd { "allan-alarm cancel it...pit $pit" }
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val it = pit ?: fetchPendingIntent(context)
        if(it != null) alarmMgr.cancel(it)
        AppDataStore.remove<Long>("autoFsTargetTs")
        targetTsData.setValueSafe(-1L)
    }

    private fun startAlarm(calendar: Calendar, context: Context): TimeInfo {
        val targetTs = calendar.timeInMillis
        AppDataStore.save("autoFsTargetTs" to targetTs)

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            val it = generatePendingIntent(context)
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTs, it)
            logd { "allanAlarm start alarm" }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        targetTsData.setValueSafe(targetTs)
        return TimeUtil.timeInfo(calendar)
    }

    fun startAlarmWhenClick(context:Context, hour:Int, min:Int, plusDay:Int) : TimeInfo?{
        val calendar = TimeUtil.hourMinuteToCalendar(hour, min, plusDay)
        if (calendar.timeInMillis <= System.currentTimeMillis() + 15_000L) {
            return null
        }
        return startAlarm(calendar, context)
    }

    fun startAlarmWhenLoop(context:Context) {
        val targetTs = AppDataStore.readBlocked("autoFsTargetTs", -1L)
        if (targetTs > 0L) {
            startAlarm(TimeUtil.targetTsToNextDayCalendar(targetTs), context)
        }
    }
}