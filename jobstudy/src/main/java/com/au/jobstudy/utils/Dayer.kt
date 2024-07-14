package com.au.jobstudy.utils

import com.au.jobstudy.utils.WeekDateUtil.anyDayToWeekStartDay
import com.au.jobstudy.utils.WeekDateUtil.timeToDayInt

/**
 * @author au
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

    override fun equals(other: Any?): Boolean {
        if (other is Dayer) {
            if (other.currentDayInt == currentDayInt) {
                return true
            }
            return false
        } else {
            throw IllegalArgumentException("Error equals dayer !!")
        }
    }

    override fun hashCode(): Int {
        return currentDayInt
    }
}