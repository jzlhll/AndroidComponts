package com.au.jobstudy.consts

import com.au.jobstudy.consts.WeekDateUtil.anyDayToWeekStartDay
import com.au.jobstudy.consts.WeekDateUtil.timeToDayInt

/**
 * @author allan
 * @date :2023/11/27 10:48
 * @description:
 */
class Dayer {
    lateinit var currentDay:String
    var currentDayInt:Int = 0
    var weekStartDayInt:Int = 0
    lateinit var weekStartDay:String

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
        currentDay = anyDay.toString()
        currentDayInt = anyDay

        weekStartDay = anyDayToWeekStartDay(currentDay)
        weekStartDayInt = weekStartDay.toInt()
    }
}