package com.au.jobstudy.utils

import com.au.jobstudy.utils.WeekDateUtil.anyDayToWeekStartDay
import com.au.jobstudy.utils.WeekDateUtil.getYesterday
import com.au.jobstudy.utils.WeekDateUtil.lastWeekStartDay
import com.au.jobstudy.utils.WeekDateUtil.timeToDayInt

/**
 * @author au
 * @date :2023/11/27 10:48
 * @description:
 */
class Dayer {
    var yesterday: Int = 0
    var currentDay:Int = 0
    var weekStartDay:Int = 0
    var lastWeekStartDay = 0

    fun isYesterdayIsLastWeek() : Boolean {
        return anyDayToWeekStartDay(yesterday) == lastWeekStartDay
    }

    constructor() {
        init(timeToDayInt())
    }

    constructor(anyDay:String) {
        init(anyDay.toInt())
    }

    constructor(anyDay:Int) {
        init(anyDay)
    }

    private fun init(anyDay: Int) {
        currentDay = anyDay
        yesterday = getYesterday(currentDay)
        weekStartDay = anyDayToWeekStartDay(anyDay)
        lastWeekStartDay = lastWeekStartDay(weekStartDay)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Dayer) {
            return other.currentDay == currentDay
        } else {
            throw IllegalArgumentException("Error equals dayer !!")
        }
    }

    override fun hashCode(): Int {
        return currentDay
    }
}