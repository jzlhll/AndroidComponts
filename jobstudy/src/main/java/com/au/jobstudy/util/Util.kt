package com.au.jobstudy.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DAY_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
private const val START_DAY = 20231127

fun timeToDay(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time))


fun timeToWeekStartDay(time:Long = System.currentTimeMillis()) : String {
    val curDay = DAY_FORMAT.format(Date(time))
    val curDayInt = curDay.toInt()
    val weekStartDay = curDayInt - ((curDayInt - START_DAY) % 7)
    return "$weekStartDay"
}
