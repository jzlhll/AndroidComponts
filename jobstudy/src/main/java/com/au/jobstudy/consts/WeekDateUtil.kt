package com.au.jobstudy.consts

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeekDateUtil {
    private val DAY_FORMAT_HHMMSS = SimpleDateFormat("HH_mm_ss_SSS", Locale.getDefault())
    private val DAY_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val DAY_FORMAT_HHMM = SimpleDateFormat("HHmm", Locale.getDefault())

    fun currentHHmmssSSS() : String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(System.currentTimeMillis())
        return DAY_FORMAT_HHMMSS.format(calendar.time)
    }

    fun offsetDayTs(day:Int) = day * (3600L * 24 * 1000)

    fun longToDate(timestamp: Long) = Date(timestamp)

    fun longToDate() = Date(System.currentTimeMillis())

    fun longToCalendar(timestamp: Long): Calendar {
        val timeStampCalendar = Calendar.getInstance()
        timeStampCalendar.timeInMillis = timestamp
        return timeStampCalendar
    }

    fun longToCalendar(): Calendar {
        val timeStampCalendar = Calendar.getInstance()
        timeStampCalendar.timeInMillis = System.currentTimeMillis()
        return timeStampCalendar
    }

    fun getYesterday(day:String) = getYesterday(DAY_FORMAT.parse(day)!!)

    fun getYesterday(dataTime: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = dataTime
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return DAY_FORMAT.format(calendar.time)
    }

    fun getTomorrow(day:String) : String{
        val date = DAY_FORMAT.parse(day)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return DAY_FORMAT.format(calendar.time)
    }

    fun getFirstWeekData(dataTime: Date): String {
        /**
         * 转为calendar格式
         * calendar.get(Calendar.MONTH)+1  calendar中的月份以0开头
         * Calendar.DAY_OF_WEEK 当前日期是所在周的第几天（以周日为一周的第一天）
         * Calendar.DATE 当前日期是几号
         */
        val week: MutableList<String> = ArrayList(8)
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
        return DAY_FORMAT.format(calendar.time)
    }

    fun getWeekData(anyDay:String) = getWeekData(DAY_FORMAT.parse(anyDay)!!)

    fun getWeekData(dataTime: Date): List<String> {
        val week: MutableList<String> = ArrayList(8)
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
            week.add(DAY_FORMAT.format(calendar.time))
        }
        return week
    }

    fun timeToDayInt(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time)).toInt()

    fun timeToDay(time:Long = System.currentTimeMillis()) = DAY_FORMAT.format(Date(time))

    fun currentDay() = DAY_FORMAT.format(Date(System.currentTimeMillis()))

    fun timeToWeekStartDay(time:Long? = null) = getFirstWeekData(Date(time ?: System.currentTimeMillis()))

    /**
     * anyDay必须是20231201的格式。即yyyyMMdd
     */
    fun anyDayToWeekStartDay(anyDay:String) = getFirstWeekData(DAY_FORMAT.parse(anyDay)!!)

    fun currentTimeToHelloGood() : String {
        val time = DAY_FORMAT_HHMM.format(Date(System.currentTimeMillis())).toInt()
        if (time >= 2230 || time <= 645) {
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
}