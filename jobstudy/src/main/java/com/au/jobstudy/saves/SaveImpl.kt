package com.au.jobstudy.saves

import com.au.aulitesql.dao.EntityListDao
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.fillDataItemMode
import com.au.jobstudy.bean.randomGetTwoSubjects
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.util.anyDayToWeekStartDay
import com.au.module_android.utils.awaitOnIoThread
import java.lang.RuntimeException
import kotlin.coroutines.resume

class SaveImpl : ISave {
    private val allWeeksDao = HashMap<String, EntityListDao<DataItem>>()

    private suspend fun generateWeekData(weekStartDay: String) : List<DataItem> {
        val weekDao =
            if (allWeeksDao.containsKey(weekStartDay))
                allWeeksDao[weekStartDay]!!
            else
                EntityListDao(DataItem::class.java)

        return awaitOnIoThread { cancellableContinuation->
            weekDao.loadAllFilter("weekStartDay", weekStartDay) {
                val weekStartDayInt = weekStartDay.toInt()
                val list = ArrayList<DataItem>()
                for (i in 0 until  7) {
                    val aday = (weekStartDayInt + i).toString()

                    val twoSubjects = randomGetTwoSubjects()
                    val firstCheck = twoSubjects[0].randomOneAction()
                    val secondCheck = twoSubjects[0].randomOneAction()

                    val dataItem1 = DataItem().apply {
                        day = aday
                        this.weekStartDay = weekStartDay
                        orderIndex = 0
                        subject = twoSubjects[0].name
                        fillDataItemMode(this, firstCheck)
                    }
                    val dataItem2 = DataItem().apply {
                        day = aday
                        this.weekStartDay = weekStartDay
                        orderIndex = 1
                        subject = twoSubjects[1].name
                        fillDataItemMode(this, secondCheck)
                    }
                    list.add(dataItem1)
                    list.add(dataItem2)
                }

                weekDao.saveAll(list) {
                    allWeeksDao[weekStartDay] = weekDao
                    cancellableContinuation.resume(list)
                }
            }
        }

    }

    override suspend fun getWeekData(day:String) : List<DataItem> {
        val dayer = Dayer(day)
        val dayWeekStart = dayer.weekStartDay
        //load from memory
        if(allWeeksDao.containsKey(dayWeekStart)) {
            allWeeksDao[dayWeekStart]!!.data?.let {
                return it
            }
        }

        //新生成
        return generateWeekData(dayWeekStart)
    }

    override suspend fun updateOneDay(item: DataItem): Boolean {
        val dayer = Dayer(item.day)
        val dayWeekStart = dayer.weekStartDay
        if (allWeeksDao.containsKey(dayWeekStart)) {
            val weekData = allWeeksDao[dayWeekStart]!!
            return awaitOnIoThread {
                weekData.update(item) {suc->
                    it.resume(suc)
                }
            }
        }

        throw RuntimeException("update OnDay() No data in allWeeksDao.")
    }

    override suspend fun deleteOneDay(day: String): Boolean {
        val dayWeekStart = anyDayToWeekStartDay(day)
        if (allWeeksDao.containsKey(dayWeekStart)) {
            val weekEntity = allWeeksDao[dayWeekStart]
            if (weekEntity != null) {
                weekEntity.data?.let { weekData->
                    val needDeleteList = weekData.filter { it.day == day }
                    return awaitOnIoThread {
                        weekEntity.deleteAll(needDeleteList) { sucCount->
                            it.resume(sucCount > 0)
                        }
                    }
                }
            }
        }
        return true
    }

    override suspend fun deleteOneWeek(day: String): Boolean {
        val dayWeekStart = anyDayToWeekStartDay(day)
        if (allWeeksDao.containsKey(dayWeekStart)) {
            awaitOnIoThread<Boolean> {
                allWeeksDao[dayWeekStart]?.clear { suc->
                    it.resume(suc)
                }
            }
        }

        return true
    }

    override suspend fun getDay(day: String) : List<DataItem>{
        val weekData = getWeekData(day)
        return weekData.filter { it.day == day }
    }
}