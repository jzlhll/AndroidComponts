package com.allan.autoclickfloat.activities.autofs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.Keep
import com.au.module_android.json.fromJsonList
import com.au.module_android.json.toJsonString
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import com.au.module_androidui.toast.toastOnTop
import com.au.module_cached.AppDataStore
import java.util.Calendar
import java.util.Random
import java.util.UUID

@Keep
data class TargetTs(val autoFsId:String, var targetTs:Long, var isLoop:Boolean, var isClose:Boolean)

/**
 * @author allan
 * @date :2024/9/25 15:36
 * @description:
 */
object AutoFsObj {
    val targetTsListData = NoStickLiveData<List<TargetTs>>()

    fun init() {
        val targetTsListJsonStr = AppDataStore.readBlocked("targetTsList", "")
        if (targetTsListJsonStr.isNotEmpty()) {
            val targetTsList = targetTsListJsonStr.fromJsonList<TargetTs>()
            targetTsListData.setValueSafe(targetTsList)
        } else {
            targetTsListData.setValueSafe(listOf())
        }
    }

    private val REQUEST_CODE = 118

    private fun generatePendingIntent(context: Context) : PendingIntent {
        val intent: Intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction("com.autoStartFs.ACTION_ALARM_TRIGGERED")
        intent.setPackage(context.packageName)
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
        intent.setPackage(context.packageName)
        val flag = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  // 必须与设置时一致
            intent,
            flag
        )
    }

    fun cancelAlarmOnly(context:Context, pit: PendingIntent?) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val it = pit ?: fetchPendingIntent(context)
        if (it != null) {
            logd { "allanAlarm first close old alarm" }
            alarmMgr.cancel(it)
        } else {
            logd { "allanAlarm first close old alarm but no" }
        }
    }

    fun removeAlarmUi(context:Context, autoFsId:String) {
        logd { "allan-alarm cancelAlarm Ui autoFsId $autoFsId" }
        val targetTsList = targetTsListData.realValue?.toMutableList() ?: return
        val foundTargetTs = targetTsList.find { it.autoFsId == autoFsId }
        if (foundTargetTs != null) {
            targetTsList.remove(foundTargetTs)
            val json = targetTsList.toJsonString()
            AppDataStore.save("targetTsList", json)
            targetTsListData.setValueSafe(targetTsList)

            checkAndStartNextAlarm(context)
        }
    }

    /**
     * 返回true表示接受本次切换。false表示不存在或者有问题
     */
    fun switchAlarmUi(context: Context, autoFsId: String, isClose:Boolean) : Boolean?{
        logd { "allan-alarm switchAlarm Ui autoFsId $autoFsId" }
        val targetTsList = targetTsListData.realValue?.toMutableList() ?: return null
        val foundTargetTs = targetTsList.find { it.autoFsId == autoFsId }
        if (foundTargetTs == null) {
            return null
        }

        if (!isClose && !foundTargetTs.isLoop && foundTargetTs.targetTs < System.currentTimeMillis()) {
            toastOnTop("该闹钟已经过期，请自行删除。") //todo支持编辑或者恢复。
            return false
        }

        if (foundTargetTs.isClose != isClose) {
            foundTargetTs.isClose = isClose
            val json = targetTsList.toJsonString()
            AppDataStore.save("targetTsList", json)
            targetTsListData.setValueSafe(targetTsList)
            checkAndStartNextAlarm(context)
        }
        return true
    }

    /**
     * 返回false表示过期的时间
     */
    fun addAlarmUiAndCheckStart(context:Context, hour:Int, min:Int, plusDay:Int, isLoop:Boolean) : Boolean{
        val calendar = TimeUtil.hourMinuteToCalendar(hour, min, plusDay)
        if (calendar.timeInMillis <= System.currentTimeMillis() + 15_000L && !isLoop) {
            return false
        }

        val targetTs = calendar.timeInMillis
        val newList = targetTsListData.realValue?.toMutableList() ?: mutableListOf()

        val autoFsId = UUID.randomUUID().toString().replace("-", "") + "_" + TimeUtil.timeYMHMS(targetTs) + if(isLoop) "loop" else "noLoop"
        val found = newList.find { it.autoFsId == autoFsId }
        if (found == null) { //确认是新增
            newList.add(TargetTs(autoFsId, targetTs, isLoop, false))
            if (false) { //不做保存。交给checkAndStartNextAlarm里面二次处理。
                val json = newList.toJsonString()
                AppDataStore.save("targetTsList", json)
            }
            targetTsListData.setValueSafe(newList)
        } else {
            throw RuntimeException("不可能有重复的autoFsId")
        }

        checkAndStartNextAlarm(context)
        return true
    }

    fun checkAndStartNextAlarm(context:Context) {
        targetTsListData.realValue?.toMutableList()?.let { newList ->
            val curTs = System.currentTimeMillis()
            val changeList = mutableListOf<TargetTs>()
            newList.forEach {
                if (it.isClose) {
                    changeList.add(it)
                } else {
                    if (it.isLoop) {
                        if (it.targetTs <= curTs) {
                            it.targetTs += 3600 * 24 * 1000L
                        }
                        it.isClose = false
                        changeList.add(it)
                    } else {
                        if (it.targetTs <= curTs) {
                            it.isClose = true
                        }
                        changeList.add(it)
                    }
                }
            }

            changeList.sortBy { it.targetTs }
            val json = changeList.toJsonString()
            AppDataStore.save("targetTsList", json)
            targetTsListData.setValueSafe(changeList)

            //先关闭
            cancelAlarmOnly(context, null)
            //开始最近一个闹钟
            if (changeList.isNotEmpty()) {
                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val ts = changeList.find { !it.isClose }?.targetTs
                if (ts != null) {
                    val log = TimeUtil.timeYMHMS(Calendar.getInstance().also { it.timeInMillis = ts })
                    try {
                        val it = generatePendingIntent(context)
                        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ts, it)
                        logd { "allanAlarm start next alarm time: $log" }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                } else {
                    logd{"allan start next alarm time: alarm all isClose."}
                }
            } else {
                logd{"allan start next alarm time: no alarms."}
            }
        }
    }
}