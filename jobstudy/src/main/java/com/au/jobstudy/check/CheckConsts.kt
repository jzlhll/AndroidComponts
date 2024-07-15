package com.au.jobstudy.check

import com.au.jobstudy.check.api.AbsGeneratorApi
import com.au.jobstudy.check.api.SummerGeneratorApi
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module.cached.AppDataStore
import com.au.module_android.simplelivedata.RealMutableLiveData
import com.au.module_android.simplelivedata.Status
import kotlinx.coroutines.delay

/**
 * statusMode 1表示work更新；2表示completed更新。3表示一起更新。
 */
data class UpdateChangeStatus(val statusMode:Int, val index:Long)

object CheckConsts {

    /**
     * 可以用来监听更新日期显示，更新上下午变化等等。
     */
    val dayerLiveData = RealMutableLiveData<Dayer>()

    private val api : AbsGeneratorApi = SummerGeneratorApi()

    var curWeekWorks:List<WorkEntity>? = null
    var lastWeekWorks:List<WorkEntity>? = null

    val statusChangedLiveData = RealMutableLiveData<UpdateChangeStatus>()

    var todayCompletedWorks: List<CompletedEntity>? = null
    var yesterdayCompletedWorks: List<CompletedEntity>? = null

    /**
     * 必须保证已经有值了才能调用
     */
    fun todayWorks() = curWeekWorks?.filter { it.day == dayerLiveData.realValue.currentDay }

    /**
     * 必须保证已经有值了才能调用
     */
    fun yesterdayWorks() = curWeekWorks?.filter { it.day == dayerLiveData.realValue.yesterday }

    /**
     * 从liveData中得到今天还有什么没有做的。
     */
    fun todayUncompletedWorks() : List<WorkEntity>?{
        val completedWorkIds = todayCompletedWorks?.map { it.dayWorkId }
        if (completedWorkIds == null) {
            return todayWorks() //一个都没有完成，直接完成。
        }
        return todayWorks()?.filter { !completedWorkIds.contains(it.id) }
    }

    /**
     * 从liveData中得到昨天还有什么没有做的。
     */
    fun yesterdayUncompletedWorks() : List<WorkEntity>? {
        val completedWorkIds = yesterdayCompletedWorks?.map { it.dayWorkId }
        if (completedWorkIds == null) {
            return yesterdayWorks() //一个都没有完成，直接完成。
        }
        return yesterdayWorks()?.filter { !completedWorkIds.contains(it.id) }
    }

    /**
     * 从liveData中得到本周还有什么没有做的。
     */
    fun thisWeekUncompletedWorks() : List<WorkEntity>{
        TODO()
    }

    /**
     * 当进入前台或者一些想检测变化日期的时候，校验结果。
     */
    suspend fun whenTrigger() {
        delay(0)

        val newDayer = Dayer()
        val curDayer = dayerLiveData.realValue
        if (curDayer == null) { //为空就进行db的读取，看看是否已经生成
            weekChanged(newDayer)
        } else {
            if (newDayer != curDayer) {
                val newWeekStartDay = WeekDateUtil.anyDayToWeekStartDay(newDayer.weekStartDay)
                if (newWeekStartDay == newDayer.weekStartDay) {
                    //只是变了一周内的一天。更新today和yesterday即可。
                    inWeekChangeDay(newDayer)
                } else {
                    //变周了
                    weekChanged(newDayer)
                }
            }
        }
        dayerLiveData.postValue(newDayer)
    }

    private suspend fun weekChanged(newDayer: Dayer) {
        val thisWeekData = getOrCreateDbWeekData(newDayer.weekStartDay)
        val lastWeekData = getOrCreateDbWeekData(newDayer.lastWeekStartDay)
        curWeekWorks = thisWeekData
        lastWeekWorks = lastWeekData

        workChangedLiveData.postValue((workChangedLiveData.realValue ?: 0) + 1)

        inWeekChangeDay(newDayer)

        statusChangedLiveData.postValue(Status(3, (statusChangedLiveData.realValue.index ?: 0) + 1))
    }

    private suspend fun inWeekChangeDay(newDayer: Dayer) {
        val todayCompleted = getDbTodayCompletedWorks(newDayer.currentDay)
        val yesterdayCompleted = getDbTodayCompletedWorks(newDayer.yesterday)
        todayCompletedWorks = (todayCompleted)
        yesterdayCompletedWorks = (yesterdayCompleted)

        completedChangedLiveData.postValue((completedChangedLiveData.realValue ?: 0) + 1)
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

    private suspend fun getDbTodayCompletedWorks(day:Int) : List<CompletedEntity>{
        delay(0)
        return AppDatabase.db.getCompletedDao().queryCompletedByDay(day)
    }

    const val SELF_STAR_COUNT_KEY = "selfStarCount"
    const val SELF_DING_COUNT_KEY = "selfDingCount"

    fun markCompleted(completedEntity: CompletedEntity) {
        val dao = AppDatabase.db.getCompletedDao()
        dao.insert(completedEntity)
        AppDataStore.save(SELF_STAR_COUNT_KEY, AppDataStore.readBlocked(SELF_STAR_COUNT_KEY, 0) + 1)
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