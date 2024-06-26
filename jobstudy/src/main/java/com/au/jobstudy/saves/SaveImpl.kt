package com.au.jobstudy.saves

import com.au.aulitesql.dao.EntityListDao
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.fillDataItemMode
import com.au.jobstudy.bean.randomGetSubject
import com.au.jobstudy.bean.randomGetTwoSubjects
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.consts.WeekDateUtil
import com.au.module_android.utils.awaitOnIoThread
import com.au.module_android.utils.logd
import java.lang.RuntimeException
import java.util.Collections
import kotlin.coroutines.resume

class SaveImpl : ISave {
    /**
     * 在这里面的就是已经与db保持了同步的数据结构。因此，后续不需要二次打开去生成。
     */
    private val loadedWeekDaos = HashMap<String, EntityListDao<DataItem>>()

    private suspend fun loadFromDb(weekStartDay: String, generate:Boolean) : List<DataItem> {
        logd { "generate OrLoad FromDb...." }
        val weekDao =
            if (loadedWeekDaos.containsKey(weekStartDay))
                loadedWeekDaos[weekStartDay]!!
            else
                EntityListDao(DataItem::class.java).also {
                    loadedWeekDaos[weekStartDay] = it
                }

        return awaitOnIoThread { cancellableContinuation->
            logd { "generate OrLoad FromDb awaitOnIoThread...."}
            cancellableContinuation.invokeOnCancellation {
                cancellableContinuation.resume(Collections.emptyList())
            }

            weekDao.loadAllFilter("weekStartDay", weekStartDay) { dbList->
                logd { "generate OrLoad FromDb awaitOnIoThread11...."}
                if (dbList.isNotEmpty()) {
                    cancellableContinuation.resume(dbList)
                } else if (generate) {
                    val targetList = ArrayList<DataItem>()
                    val weekDayList = WeekDateUtil.getWeekData(weekStartDay)
                    var index = 0
                    for (aday in weekDayList) {
                        index++
                        val subjects = if (index <= 5) {
                            val rand = Math.random()
                            if(rand < 0.1f) //周内小概率0个作业。
                                arrayOf()
                            else if (rand >= 0.9f) { //周内小概率2个作业。
                                randomGetTwoSubjects()
                            } else { //周内大概率1个作业。
                                arrayOf(randomGetSubject())
                            }
                        } else {
                            randomGetTwoSubjects()
                        }

                        if (subjects.isNotEmpty()) {
                            val firstCheck = subjects[0].randomOneAction()

                            val dataItem1 = DataItem().apply {
                                day = aday
                                this.weekStartDay = weekStartDay
                                orderIndex = 0
                                subject = subjects[0].name
                                fillDataItemMode(this, firstCheck)
                            }
                            targetList.add(dataItem1)
                        }

                        if (subjects.size == 2) {
                            val secondCheck = subjects[1].randomOneAction()
                            val dataItem2 = DataItem().apply {
                                day = aday
                                this.weekStartDay = weekStartDay
                                orderIndex = 1
                                subject = subjects[1].name
                                fillDataItemMode(this, secondCheck)
                            }
                            targetList.add(dataItem2)
                        }
                    }

                    weekDao.saveAll(targetList) {
                        cancellableContinuation.resume(targetList)
                    }
                } else {
                    cancellableContinuation.resume(Collections.emptyList())
                }
            }
        }
    }

    override fun isLoadedWeek(anyDay: String) = loadedWeekDaos.containsKey(Dayer(anyDay).weekStartDay)

    private fun getWeekDao(weekStartDay: String) = loadedWeekDaos[weekStartDay]!!

    override suspend fun loadWeekData(anyDay: String, notExistGenerate: Boolean): List<DataItem> {
        val dayer = Dayer(anyDay)
        val weekStartDay = dayer.weekStartDay
        //新生成 or 从db中提取. 不论如何都提取一次
        return loadFromDb(weekStartDay, notExistGenerate)
    }

    //later 自行确保调用之前已经load了WeekData
    override fun getWeekData(anyDay: String): List<DataItem>? {
        val dayer = Dayer(anyDay)
        val weekStartDay = dayer.weekStartDay
        return getWeekDao(weekStartDay).data
    }

    override suspend fun updateOneDay(item: DataItem): Boolean {
        val dayer = Dayer(item.day)
        val dayWeekStart = dayer.weekStartDay

        if (!isLoadedWeek(dayWeekStart)) {
            throw RuntimeException("updateOneDay please call getWeekData before.")
        }

        return awaitOnIoThread {
            getWeekDao(dayWeekStart).update(item) {suc->
                it.resume(suc)
            }
        }
    }

    override suspend fun deleteOneDay(day: String): Boolean {
        val dayWeekStart = WeekDateUtil.anyDayToWeekStartDay(day)
        if (!isLoadedWeek(dayWeekStart)) {
            loadFromDb(dayWeekStart, false)
        }

        val weekEntity = getWeekDao(dayWeekStart)
        weekEntity.data?.let { weekData->
            val needDeleteList = weekData.filter { it.day == day }
            return awaitOnIoThread {
                weekEntity.deleteAll(needDeleteList) { sucCount->
                    it.resume(sucCount > 0)
                }
            }
        }
        return true
    }

    override suspend fun deleteOneWeek(anyDay: String): Boolean {
        val weekStartDay = WeekDateUtil.anyDayToWeekStartDay(anyDay)
        if (!isLoadedWeek(weekStartDay)) {
            loadFromDb(weekStartDay, false)
        }

        val weekEntity = getWeekDao(weekStartDay)
        weekEntity.data?.let { weekData->
            return awaitOnIoThread {
                weekEntity.deleteAll(weekData) { sucCount->
                    it.resume(sucCount > 0)
                }
            }
        }
        return true
    }

    //later 自行确保调用之前已经load了WeekData
    override fun getDay(day: String) : List<DataItem>?{
        val dayer = Dayer(day)
        val weekStartDay = dayer.weekStartDay

        return getWeekDao(weekStartDay).data?.filter {
            it.day == day
        }
    }
}