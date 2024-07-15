package com.au.jobstudy.check.api

import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.utils.WeekDateUtil
import kotlinx.coroutines.delay

abstract class AbsGeneratorApi {
    suspend fun getWeekWorks(weekStartDay:Int) : List<WorkEntity> {
        delay(0)
        val days = WeekDateUtil.getWeekData(weekStartDay)
        val newList = ArrayList<WorkEntity>()
        days.forEach { day->
            newList.addAll(createADayWorkEntities(weekStartDay, day))
        }
        newList.addAll(createWeekWorkEntities(weekStartDay))
        return newList
    }

    protected abstract fun createADayWorkEntities(weekStartDay: Int, day:Int) : List<WorkEntity>
    protected abstract fun createWeekWorkEntities(weekStartDay: Int) : List<WorkEntity>
}