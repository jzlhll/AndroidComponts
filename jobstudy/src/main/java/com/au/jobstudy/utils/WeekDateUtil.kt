package com.au.jobstudy.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeekDateUtil {
    private val DAY_FORMAT_HHMMSS = SimpleDateFormat("HH_mm_ss_SSS", Locale.CHINA)
    private val DAY_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val DAY_FORMAT_HHMM = SimpleDateFormat("HHmm", Locale.CHINA)
//
    fun currentHHmmssSSS() : String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(System.currentTimeMillis())
        return DAY_FORMAT_HHMMSS.format(calendar.time)
    }
//
//    fun longToDate(timestamp: Long) = Date(timestamp)
//
//    fun longToDate() = Date(System.currentTimeMillis())
//
//    fun longToCalendar(timestamp: Long): Calendar {
//        val timeStampCalendar = Calendar.getInstance()
//        timeStampCalendar.timeInMillis = timestamp
//        return timeStampCalendar
//    }
//
//    fun longToCalendar(): Calendar {
//        val timeStampCalendar = Calendar.getInstance()
//        timeStampCalendar.timeInMillis = System.currentTimeMillis()
//        return timeStampCalendar
//    }

    fun getYesterday(day:Int) = getYesterday(DAY_FORMAT.parse("" + day)!!)

    private fun getYesterday(dataTime: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = dataTime
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return DAY_FORMAT.format(calendar.time).toInt()
    }

    fun getTomorrow(day:Int) : Int{
        val date = DAY_FORMAT.parse("" + day)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return DAY_FORMAT.format(calendar.time).toInt()
    }

    private fun getFirstWeekData(dataTime: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = dataTime

        // 如果是周日
        if (calendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        // 获取当前日期是当周的第i天
        val i = calendar[Calendar.DAY_OF_WEEK] - 1

        // 获取当前日期所在周的第一天
        calendar.add(Calendar.DATE, -i + 1)
        return DAY_FORMAT.format(calendar.time).toInt()
    }

    /**
     * 获取一周的日期
     */
    fun getWeekData(anyDay:Int) = getWeekData(DAY_FORMAT.parse("" + anyDay)!!)

    private fun getWeekData(dataTime: Date): List<Int> {
        val week: MutableList<Int> = ArrayList(8)
        val calendar = Calendar.getInstance()
        calendar.time = dataTime

        // 如果是周日
        if (calendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        // 获取当前日期是当周的第i天
        val i = calendar[Calendar.DAY_OF_WEEK] - 1

        // 获取当前日期所在周的第一天
        calendar.add(Calendar.DATE, -i + 1)
        for (j in 0..6) {
            if (j > 0) {
                calendar.add(Calendar.DATE, 1)
            }
            week.add(DAY_FORMAT.format(calendar.time).toInt())
        }
        return week
    }

    fun timeToDayInt(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time)).toInt()
//
//    fun timeToDay(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time))
//
//    fun currentDay() = DAY_FORMAT.format(Date(System.currentTimeMillis()))
//
//    fun timeToWeekStartDay(time:Long? = null) = getFirstWeekData(Date(time ?: System.currentTimeMillis()))

    /**
     * anyDay必须是20231201的格式。即yyyyMMdd
     */
    fun anyDayToWeekStartDay(anyDay:Int) = getFirstWeekData(DAY_FORMAT.parse("" + anyDay)!!)

    /**
     * 传入这周的起始日期，得到上周的起始日期
     */
    fun lastWeekStartDay(weekStartDay: Int) = getFirstWeekData(DAY_FORMAT.parse("" + getYesterday(weekStartDay))!!)

    fun currentTimeToHelloGood() : String {
        val time = DAY_FORMAT_HHMM.format(Date(System.currentTimeMillis())).toInt()
        if (time >= 2200 || time <= 700) {
            return "夜深了，注意休息"
        }

        if (time <= 1115) {
            return "上午好"
        }

        if (time <= 1400) {
            return "中午好"
        }

        if (time <= 1815) {
            return "下午好"
        }

        return "晚上好"
    }

    fun getTodayWeekN() : String{
        // 获取当前时间
        val now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        // 格式化时间
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // 获取周几
        val dayOfWeek = now.dayOfWeek.value // 周一为1, 周日为7
        val s = when (dayOfWeek) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> ""
        }
        return now.format(formatter) + " $s"
    }

    fun getDayOfWeek(day: Int): String {
        val date = DAY_FORMAT.parse("$day")!!
        val calendar = Calendar.getInstance()
        calendar.time = date

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> "未知的星期"
        }
    }

    fun checkDateIsTodayOrYesterday(day: Int): String? {
        val inputDate: Date = DAY_FORMAT.parse("$day")!!

        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Reset the calendar to today 00:00:00
        calendar.time = today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayMidnight = calendar.time

        // Calculate yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayMidnight = calendar.time

        return when {
            inputDate >= todayMidnight -> "今天"
            inputDate >= yesterdayMidnight -> "昨天"
            else -> null
        }
    }

    /**
     * 返回现在是周几。1~7代表周一到周日。
     */
    fun getTodayWeekIndex() : Int{
        // 获取当前时间
        val now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        // 获取周几
        val dayOfWeek = now.dayOfWeek.value // 周一为1, 周日为7
        return dayOfWeek
    }
}