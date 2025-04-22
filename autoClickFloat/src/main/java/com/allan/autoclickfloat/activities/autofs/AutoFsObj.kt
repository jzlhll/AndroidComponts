package com.allan.autoclickfloat.activities.autofs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.allan.autoclickfloat.AllPermissionActivity
import com.au.module_android.Globals
import com.au.module_android.json.fromJsonList
import com.au.module_android.json.toJsonString
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import com.au.module_android.utils.loge
import com.au.module_cached.AppDataStore
import com.au.module_cached.delegate.AppDataStoreLongCache
import okhttp3.internal.toImmutableList
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

@Keep
data class TargetTs(val autoFsId:String, var targetTs:Long, var isLoop:Boolean, var isClose:Boolean, var offsetMinute:Int)

/**
 * @author allan
 * @date :2024/9/25 15:36
 * @description:
 */
object AutoFsObj {
    val targetTsListData = NoStickLiveData<List<TargetTs>>()
    private var isInited = false

    fun init(context: Context, reason:String = "") {
        if(isInited) return
        isInited = true
        val targetTsListJsonStr = AppDataStore.readBlocked("targetTsList", "")
        if (targetTsListJsonStr.isNotEmpty()) {
            val targetTsList = targetTsListJsonStr.fromJsonList<TargetTs>()
            targetTsListData.setValueSafe(targetTsList)
        } else {
            targetTsListData.setValueSafe(listOf())
        }

        if (reason == "boot") {
            checkAndStartNextAlarm(context)
        } else {
            Globals.mainHandler.postDelayed({
                checkAndStartNextAlarm(context)
            }, 3000)
        }
    }

    private const val REQUEST_CODE = 118
    private const val SHOW_UI_CODE = 119

    private const val ACTION_TRIGGER = "com.autoStartFs.ACTION_ALARM_TRIGGERED"
    private const val ACTION_JUMP = "com.autoStartFs.ACTION_ALARM_JUMP"

    const val EXTRA_TARGET_TS_LONG = "targetTsLong"
    const val EXTRA_TARGET_TS_INFO = "targetTsInfo"

    private fun generatePendingIntent(context: Context, targetTs: Long, targetTsInfo:String) : PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction(ACTION_TRIGGER)
        intent.setPackage(context.packageName)
        intent.putExtra(EXTRA_TARGET_TS_LONG, targetTs)
        intent.putExtra(EXTRA_TARGET_TS_INFO, targetTsInfo)
        val flag = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,  // 必须与设置时一致
            intent,
            flag
        )
    }

    fun fetchPendingIntent(context: Context) : PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction(ACTION_TRIGGER)
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

    fun isAlarmExpired(autoFsId: String) : Boolean{
        val targetTsList = targetTsListData.realValue?.toMutableList() ?: return false
        val foundTargetTs = targetTsList.find { it.autoFsId == autoFsId } ?: return false
        return !foundTargetTs.isLoop && foundTargetTs.targetTs < System.currentTimeMillis()
    }

    /**
     * 返回""表示接受本次切换。"not found", "expired"表示不存在或者有问题
     */
    fun switchAlarmUi(context: Context, autoFsId: String, isClose:Boolean) : String{
        logd { "allan-alarm switchAlarm Ui autoFsId $autoFsId" }
        val targetTsList = targetTsListData.realValue?.toMutableList() ?: return "not found"
        val foundTargetTs = targetTsList.find { it.autoFsId == autoFsId }
        if (foundTargetTs == null) {
            return "not found"
        }

        if (!isClose && !foundTargetTs.isLoop && foundTargetTs.targetTs < System.currentTimeMillis()) {
            return "expired"
        }

        if (foundTargetTs.isClose != isClose) {
            foundTargetTs.isClose = isClose
            val json = targetTsList.toJsonString()
            AppDataStore.save("targetTsList", json)
            targetTsListData.setValueSafe(targetTsList)
            checkAndStartNextAlarm(context)
        }
        return ""
    }

    /**
     * 还剩下N秒，就不允许设定
     */
    private const val SET_ALARM_DELTA_CURRENT = 10_000L

    /**
     * 返回false表示过期的时间
     */
    fun setAlarmUiAndCheckStart(
        context: Context,
        hour: Int,
        min: Int,
        plusDay: Int,
        withinOffsetMinute:Int,
        isLoop: Boolean,
        oldAutoFsId: String? = null
    ): Boolean {
        val calendar = TimeUtil.hourMinuteToCalendar(hour, min, plusDay)
        val currentTime = System.currentTimeMillis()

        // 时间有效性检查
        if (calendar.timeInMillis <= currentTime + SET_ALARM_DELTA_CURRENT && !isLoop) {
            return false
        }

        val targetTs = calendar.timeInMillis
        val newList = targetTsListData.realValue?.toMutableList() ?: mutableListOf()

        if (!oldAutoFsId.isNullOrEmpty()) { // 编辑模式 移除旧条目
            newList.removeIf { it.autoFsId == oldAutoFsId }
        }

        // 添加新条目（新增模式直接添加，编辑模式已确保移除旧条目）
        newList.add(TargetTs(generateId(targetTs, isLoop), targetTs, isLoop, false, withinOffsetMinute))
        targetTsListData.setValueSafe(newList)

        checkAndStartNextAlarm(context)
        return true
    }

    private fun generateId(targetTs: Long, isLoop: Boolean): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val time = TimeUtil.timeYMHMS(targetTs).replace(" ", "_")
        val autoFsId = uuid + "_" + time + if (isLoop) "_loop" else "_noLoop"
        return autoFsId
    }

    private var lastCheckAndStartSystemTs by AppDataStoreLongCache("lastCheckAndStartSystemTs", System.currentTimeMillis())
    private const val TIME_ONE_DAY = 3600 * 24 * 1000L

    private fun recoverTargetList(): List<TargetTs>? {
        val lastTs = lastCheckAndStartSystemTs
        val curTs = System.currentTimeMillis()
        if (curTs < lastTs) {
            loge { "Error check AndStart NextAlarm 不可能的curTs" }
            return null
        }

        lastCheckAndStartSystemTs = curTs

        val newList = targetTsListData.realValueUnsafe.toImmutableList()

        val changeList = mutableListOf<TargetTs>()
        newList.forEach {
            if (it.isClose) {
                changeList.add(it)
            } else {
                if (it.isLoop) {
                    while(it.targetTs <= curTs) {
                        it.targetTs += TIME_ONE_DAY
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
        return changeList
    }

    fun checkAndStartNextAlarm(context:Context) {
        logd { "allanAlarm check start:" }
        val changeList = recoverTargetList() ?: return

        val json = changeList.toJsonString()
        AppDataStore.save("targetTsList", json)
        targetTsListData.setValueSafe(changeList)

        //先关闭
        cancelAlarmOnly(context, null)
        //开始最近一个闹钟

        if (changeList.isEmpty()) {
            logd { "allan >>startNextAlarm<< time: no alarms." }
        } else {
            var offsetMinute = 0
            var ts = 0L
            changeList.find { !it.isClose }?.let {
                ts = it.targetTs
                offsetMinute = it.offsetMinute
            }
            if (ts > 0L) {
                startAlarmByTs(ts, offsetMinute, context)
            } else {
                logd { "allan >>startNextAlarm<< time: alarm all isClose." }
            }
        }
    }

//    private fun startAlarmByTs(ts:Long, context: Context) {
//        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val log = TimeUtil.timeYMHMS(Calendar.getInstance().also { it.timeInMillis = ts })
//        try {
//            val it = generatePendingIntent(context, ts, log)
//            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ts, it)
//            logd { "allanAlarm >>startNextAlarm<< time: $log" }
//        } catch (e: SecurityException) {
//            loge { "allanAlarm >>startNextAlarm<< time error: ${e.message}" }
//        }
//    }

    private fun startAlarmByTs(origTs: Long, offsetMinute: Int, context: Context) {
        val origLog = TimeUtil.timeYMHMS(Calendar.getInstance().also { it.timeInMillis = origTs })

        val ts = if (offsetMinute != 0) {
            val offsetMax = offsetMinute * 60 * 1000L
            val offsetMin = -offsetMinute * 60 * 1000L
            val offsetRandom = Random.nextLong(offsetMin, offsetMax)
            origTs + offsetRandom
        } else {
            origTs
        }

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val log = TimeUtil.timeYMHMS(Calendar.getInstance().also { it.timeInMillis = ts })

        try {
            // 生成用于实际触发操作的PendingIntent（例如启动Service）
            val triggerIntent = generatePendingIntent(context, ts, log)

            // 创建用于状态栏显示的跳转Intent（例如打开应用主界面）
            val showIntent = Intent(context, AllPermissionActivity::class.java).apply {
                action = ACTION_JUMP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("alarm", "alarmIsComingWhenNoStartActivity")
            }

            // 构建显示用PendingIntent（与触发用区分requestCode）
            val showPendingIntent = PendingIntent.getActivity(
                context,
                SHOW_UI_CODE, // 使用独立requestCode避免冲突
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 创建AlarmClockInfo对象（必须包含显示用PendingIntent）
            val alarmInfo = AlarmManager.AlarmClockInfo(ts, showPendingIntent)

            // 设置AlarmClock类型的精确闹钟
            alarmMgr.setAlarmClock(alarmInfo, triggerIntent)
            logd { "allanAlarm >>startNextAlarm V2<< time: $log origLog:$origLog" }
        } catch (e: SecurityException) {
            loge { "allanAlarm >>startNextAlarm V2<< time error: ${e.message}" }
        }
    }

    /**
     * 找到启动的activity其实就是AllPermissionActivity。
     */
    fun findLaunchActivity(context: Context): Pair<Intent, Boolean> {
        val l = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
        val className = l.component?.className
        val found = Globals.activityList.find { className?.contains(it.javaClass.simpleName) == true}
        return l to (found != null)
    }

}