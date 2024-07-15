package com.au.jobstudy.check.api

import com.au.jobstudy.check.AppDatabase
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.utils.WeekDateUtil
import kotlinx.coroutines.delay

abstract class AbsGeneratorApi {
    suspend fun getWeekWorks(weekStartDay:Int) : List<WorkEntity> {
        delay(0)
        val dao = AppDatabase.db.getDao()
        val weekStartDayInt = CheckConsts.currentDayer.weekStartDayInt
        val list = dao.queryAWeek(weekStartDayInt)
        if (list.isEmpty()) {
            val days = WeekDateUtil.getWeekData(CheckConsts.currentDayer.weekStartDay)
            val newList = ArrayList<WorkEntity>()
            days.forEach { day->
                newList.addAll(createADayWorkEntities(weekStartDayInt, day))
            }
            newList.addAll(createWeekWorkEntities(weekStartDay))
            return newList
        } else {
            return list
        }
    }

    protected abstract fun createADayWorkEntities(weekStartDay: Int, day:String) : List<WorkEntity>
    protected abstract fun createWeekWorkEntities(weekStartDay: Int) : List<WorkEntity>
}