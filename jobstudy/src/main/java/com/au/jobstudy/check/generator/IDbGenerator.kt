package com.au.jobstudy.check.generator

import com.au.jobstudy.check.AppDatabase
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.utils.WeekDateUtil

abstract class IDbGenerator {
    open fun getOrCreateWeekDb(weekStartDay:Int) : List<WorkEntity> {
        val dao = AppDatabase.db.getDao()
        val weekStartDayInt = CheckConsts.currentDayer.weekStartDayInt
        val list = dao.queryAWeek(weekStartDayInt)
        if (list.isEmpty()) {
            val days = WeekDateUtil.getWeekData(CheckConsts.currentDayer.weekStartDay)
            val newList = ArrayList<WorkEntity>()
            days.forEach { day->
                newList.addAll(createADayWorkEntity(weekStartDayInt, day))
            }
            return newList
        } else {
            return list
        }
    }

    abstract fun createADayWorkEntity(weekStartDay: Int, day:String) : List<WorkEntity>
}