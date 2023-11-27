package com.au.jobstudy.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DAY_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
private val DAY_FORMAT_HH = SimpleDateFormat("HH", Locale.getDefault())
private const val START_DAY = 20231127

fun timeToDayInt(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time)).toInt()

fun timeToDay(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time))

fun currentDay() = DAY_FORMAT.format(Date(System.currentTimeMillis()))

fun timeToWeekStartDay(time:Long = System.currentTimeMillis()) : String {
    val curDay = DAY_FORMAT.format(Date(time))
    val curDayInt = curDay.toInt()
    val weekStartDay = curDayInt - ((curDayInt - START_DAY) % 7)
    return "$weekStartDay"
}

fun anyDayToWeekStartDay(anyDay:String) : String {
    val dayInt = anyDay.toInt()
    val weekStartDay = dayInt - ((dayInt - START_DAY) % 7)
    return "$weekStartDay"
}

fun currentTimeToHello() : String{
    val time = DAY_FORMAT_HH.format(Date(System.currentTimeMillis())).toInt()
    if (time >= 23 || time <= 7) {
        return "凌晨，注意休息，"
    }

    if (time <= 11) {
        return "上午好"
    }

    if (time <= 14) {
        return "中午好"
    }

    if (time <= 18) {
        return "下午好"
    }

    return "晚上好"
}