package com.au.jobstudy.check

import com.au.jobstudy.BuildConfig
import com.au.jobstudy.check.api.AbsGeneratorApi
import com.au.jobstudy.check.api.SummerGeneratorApi
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.star.StarConsts
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_cached.AppDataStore
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import kotlinx.coroutines.delay

enum class StatusMode {
    Completed,
    All
}
/**
 * statusMode 1表示work更新；2表示completed更新。3表示一起更新。
 */
data class UpdateChangeStatus(val statusMode:StatusMode, val index:Long)

object CheckConsts {

    /**
     * 可以用来监听更新日期显示，更新上下午变化等等。
     */
    var dayer:Dayer? = null
    fun currentDay() = dayer!!.currentDay

    private val api : AbsGeneratorApi = SummerGeneratorApi()

    lateinit var curWeekWorks:List<WorkEntity>
    lateinit var lastWeekWorks:List<WorkEntity>

    val statusChangedLiveData = NoStickLiveData<UpdateChangeStatus>()

    var todayCompletedWorks: List<CompletedEntity>? = null
    var yesterdayCompletedWorks: List<CompletedEntity>? = null
    var curWeekWorksCompleted:List<CompletedEntity>? = null

    /**
     * 必须保证已经有值了才能调用
     */
    fun todayWorks() = curWeekWorks.filter { it.day == dayer!!.currentDay }

    /**
     * 必须保证已经有值了才能调用
     */
    fun yesterdayWorks() : List<WorkEntity> {
        val d = dayer!!
        if (d.isYesterdayIsLastWeek()) {
            return lastWeekWorks.filter { it.day == d.yesterday }
        }
        return curWeekWorks.filter { it.day == d.yesterday }
    }

    /**
     * 从liveData中得到今天还有什么没有做的。
     */
    fun todayUncompletedWorks() : List<WorkEntity> {
        val completedWorkIds = todayCompletedWorks?.map { it.dayWorkId }
        if (completedWorkIds.isNullOrEmpty()) {
            return todayWorks() //一个都没有完成，直接完成。
        }
        return todayWorks().filter { !completedWorkIds.contains(it.id) }
    }

    /**
     * 从liveData中得到昨天还有什么没有做的。
     */
    fun yesterdayUncompletedWorks() : List<WorkEntity> {
        val completedWorkIds = yesterdayCompletedWorks?.map { it.dayWorkId }
        if (completedWorkIds.isNullOrEmpty()) {
            return yesterdayWorks() //一个都没有完成，直接完成。
        }
        return yesterdayWorks().filter { !completedWorkIds.contains(it.id) }
    }

    /**
     * 从liveData中得到周任务
     */
    fun weeklyWorks() : List<WorkEntity> {
        return curWeekWorks.filter { it.day == 0 }
    }

    fun weeklyUncompletedWorks() : List<WorkEntity> {
        val weekCompletedIds = curWeekWorksCompleted?.map { it.dayWorkId } ?: return weeklyWorks()
        return weeklyWorks().filter { !weekCompletedIds.contains(it.id) || it.notCollapse  }
    }

    /**
     * 当进入前台或者一些想检测变化日期的时候，校验结果。
     */
    suspend fun whenTrigger(force:Boolean = false) {
        delay(0)

        logd { "when trigger" }
        val newDayer = Dayer()
        val curDayer = dayer
        dayer = newDayer

        //增加dingCount
        val savedDay = readSavedDay(newDayer.currentDay)
        if (savedDay != newDayer.currentDay) { //后面的天打开
            StarConsts.fakeUpdateStudentsDingCount()
            AppDataStore.save(SAVED_CUR_DAY, newDayer.currentDay)
        }

        //读取
        var isWeekChange = false
        if (curDayer == null) { //为空就进行db的读取，看看是否已经生成
            StarConsts.initData()
            isWeekChange = true
        } else {
            isWeekChange = if (newDayer != curDayer) {
                val newWeekStartDay = WeekDateUtil.anyDayToWeekStartDay(newDayer.weekStartDay)
                if (newWeekStartDay == newDayer.weekStartDay) {
                    //只是变了一周内的一天。更新today和yesterday即可。
                    false
                } else {
                    //变周了
                    true
                }
            } else {
                //如果是同一天。
                false
            }
        }

        if (isWeekChange || force) {
            weekChanged(newDayer)
            updateStatusChangedLiveData(true)
        } else {
            inWeekChangeDay(newDayer)
            updateStatusChangedLiveData(false)
        }
    }

    private fun updateStatusChangedLiveData(isAll:Boolean) {
        val st = if(isAll) StatusMode.All else StatusMode.Completed
        val oldIndex = (statusChangedLiveData.realValue?.index ?: 0)
        statusChangedLiveData.setValueSafe(UpdateChangeStatus(st, oldIndex + 1))
    }

    private suspend fun weekChanged(newDayer: Dayer) {
        val thisWeekData = getOrCreateDbWeekData(newDayer.weekStartDay)
        val lastWeekData = getOrCreateDbWeekData(newDayer.lastWeekStartDay)
        curWeekWorks = thisWeekData

        val workIds = weeklyWorks().map { it.id }
        logd { "weekChanged workIds $workIds" }
        curWeekWorksCompleted = AppDatabase.db.getCompletedDao().queryCompletedListByWorkIds(workIds)

        if (BuildConfig.DEBUG) {
            curWeekWorksCompleted?.forEach {
                logd { "curWeekWorksCompleted workid" }
            }
        }

        lastWeekWorks = lastWeekData

        inWeekChangeDay(newDayer)
    }

    private suspend fun inWeekChangeDay(newDayer: Dayer) {
        todayCompletedWorks = getDbCompletedWorks(newDayer.currentDay)
        yesterdayCompletedWorks = getDbCompletedWorks(newDayer.yesterday)
    }

    private suspend fun getOrCreateDbWeekData(weekStartDayInt:Int) : List<WorkEntity>{
        val dao = AppDatabase.db.getWorkDao()
        val dbList = dao.queryAWeek(weekStartDayInt)
        if (dbList.isEmpty()) {
            val works = api.getWeekWorks(weekStartDayInt)
            val longs = dao.insert(works)
            works.forEachIndexed { index, workEntity ->
                works[index].id = longs[index]
            }
            return works
        }
        return dbList
    }

    private suspend fun getDbCompletedWorks(day:Int) : List<CompletedEntity>{
        delay(0)
        return AppDatabase.db.getCompletedDao().queryCompletedByDay(day)
    }

    const val SAVED_CUR_DAY = "savedCurDay"

    suspend fun markCompleted(completedEntity: CompletedEntity, isUpdate:Boolean) {
        val dao = AppDatabase.db.getCompletedDao()
        if(isUpdate) dao.update(completedEntity) else dao.insert(completedEntity)
        logd { "mark completed id: ${completedEntity.dayWorkId}" }

        StarConsts.updateNameStar(NameList.NAMES_JIANG_TJ)

        whenTrigger(true)
    }

    fun readSavedDay(defValue:Int) : Int {
        return AppDataStore.readBlocked(SAVED_CUR_DAY, defValue)
    }
}