package com.au.jobstudy.check

import com.au.jobstudy.check.api.AbsGeneratorApi
import com.au.jobstudy.check.api.SummerGeneratorApi
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module.cached.AppDataStore
import com.au.module_android.simplelivedata.RealMutableLiveData
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
    val dayerLiveData = RealMutableLiveData<Dayer>()
    fun currentDay() = dayerLiveData.realValue.currentDay

    private val api : AbsGeneratorApi = SummerGeneratorApi()

    lateinit var curWeekWorks:List<WorkEntity>
    lateinit var lastWeekWorks:List<WorkEntity>

    val statusChangedLiveData = RealMutableLiveData<UpdateChangeStatus>()

    var todayCompletedWorks: List<CompletedEntity>? = null
    var yesterdayCompletedWorks: List<CompletedEntity>? = null
    var curWeekWorksCompleted:List<CompletedEntity>? = null

    /**
     * 必须保证已经有值了才能调用
     */
    fun todayWorks() = curWeekWorks.filter { it.day == dayerLiveData.realValue.currentDay }

    /**
     * 必须保证已经有值了才能调用
     */
    fun yesterdayWorks() : List<WorkEntity> {
        if (dayerLiveData.realValue.isYesterdayIsLastWeek()) {
            return lastWeekWorks.filter { it.day == dayerLiveData.realValue.yesterday }
        }
        return curWeekWorks.filter { it.day == dayerLiveData.realValue.yesterday }
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
        return weeklyWorks().filter { !weekCompletedIds.contains(it.id)  }
    }

    /**
     * 当进入前台或者一些想检测变化日期的时候，校验结果。
     */
    suspend fun whenTrigger() {
        delay(0)

        logd { "when trigger" }
        val newDayer = Dayer()
        val curDayer = dayerLiveData.realValue
        if (curDayer == null) { //为空就进行db的读取，看看是否已经生成
            weekChanged(newDayer)
            updateStatusChangedLiveData(true)
        } else {
            if (newDayer != curDayer) {
                val newWeekStartDay = WeekDateUtil.anyDayToWeekStartDay(newDayer.weekStartDay)
                if (newWeekStartDay == newDayer.weekStartDay) {
                    //只是变了一周内的一天。更新today和yesterday即可。
                    inWeekChangeDay(newDayer)
                    updateStatusChangedLiveData(false)
                } else {
                    //变周了
                    weekChanged(newDayer)
                    updateStatusChangedLiveData(true)
                }
            } else {
                //如果是同一天。
                inWeekChangeDay(newDayer)
                updateStatusChangedLiveData(false)
            }
        }
        dayerLiveData.postValue(newDayer)
    }

    private fun updateStatusChangedLiveData(isAll:Boolean) {
        val st = if(isAll) StatusMode.All else StatusMode.Completed
        val oldIndex = (statusChangedLiveData.realValue?.index ?: 0)
        statusChangedLiveData.postValue(UpdateChangeStatus(st, oldIndex + 1))
    }

    private suspend fun weekChanged(newDayer: Dayer) {
        val thisWeekData = getOrCreateDbWeekData(newDayer.weekStartDay)
        val lastWeekData = getOrCreateDbWeekData(newDayer.lastWeekStartDay)
        curWeekWorks = thisWeekData

        val workIds = weeklyWorks().map { it.id }
        curWeekWorksCompleted = AppDatabase.db.getCompletedDao().queryCompletedListByWorkIds(workIds)

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
            dao.insert(works)
            return works
        }
        return dbList
    }

    private suspend fun getDbCompletedWorks(day:Int) : List<CompletedEntity>{
        delay(0)
        return AppDatabase.db.getCompletedDao().queryCompletedByDay(day)
    }

    const val SELF_STAR_COUNT_KEY = "selfStarCount"
    const val SELF_DING_COUNT_KEY = "selfDingCount"

    suspend fun markCompleted(completedEntity: CompletedEntity) {
        val dao = AppDatabase.db.getCompletedDao()
        dao.insert(completedEntity)
        AppDataStore.save(SELF_STAR_COUNT_KEY, AppDataStore.readBlocked(SELF_STAR_COUNT_KEY, 0) + 1)

        whenTrigger()
    }

    fun updateMyDingCount() {
        AppDataStore.save(SELF_DING_COUNT_KEY, AppDataStore.readBlocked(SELF_DING_COUNT_KEY, 0) + 1)
    }

    fun readMyDingCount() : Int{
        return AppDataStore.readBlocked(SELF_DING_COUNT_KEY, 0)
    }

    fun readMyStarCount() : Int {
        return AppDataStore.readBlocked(SELF_STAR_COUNT_KEY, 0)
    }
}