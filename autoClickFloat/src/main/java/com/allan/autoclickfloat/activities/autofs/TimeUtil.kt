package com.allan.autoclickfloat.activities.autofs

import com.au.module_android.utils.logd
import java.util.Calendar
import java.util.Locale

class TimeUtil {
    companion object {
        fun hourMinuteToCalendar(hour:Int, min:Int, playDay:Int = 0) : Calendar {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = min
            calendar[Calendar.SECOND] = 0
            if (playDay != 0) {
                calendar.timeInMillis += playDay * 24 * 60 * 60 * 1000
            }
            return calendar
        }

        fun timeInfo(calendar: Calendar) : TimeInfo{
            // 获取年、月、日、时、分、秒
            val year: Int = calendar.get(Calendar.YEAR)
            val month: Int = calendar.get(Calendar.MONTH) + 1 // 月份从 0 开始，需要 +1
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val minute: Int = calendar.get(Calendar.MINUTE)
            val second: Int = calendar.get(Calendar.SECOND)
            // 格式化输出
            val dateTime = String.format(
                Locale.CHINA,
                "%04d-%02d-%02d %02d:%02d:%02d",  // 格式：YYYY-MM-DD HH:MM:SS
                year, month, day, hour, minute, second
            )
            logd { "allan-alarm dateTime: $dateTime" }
            return TimeInfo(dateTime, calendar.timeInMillis)
        }

        fun targetTsToNextDayCalendar(ts: Long) : Calendar{
            val c = Calendar.getInstance()
            c.timeInMillis = ts + 24 * 60 * 60 * 1000
            return c
        }
    }
}