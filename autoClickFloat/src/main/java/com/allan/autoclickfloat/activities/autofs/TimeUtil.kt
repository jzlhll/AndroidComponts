package com.allan.autoclickfloat.activities.autofs

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
            calendar[Calendar.MILLISECOND] = 0
            if (playDay != 0) {
                calendar.timeInMillis += playDay * 24 * 60 * 60 * 1000
            }
            return calendar
        }

        fun hourMinuteToCalendar2(hour:Int, min:Int, playDay:Int = 0) : Calendar {
            // 1. 获取 Calendar 实例（默认当前系统时间）
            val calendar = Calendar.getInstance()
            // 2. 设置小时和分钟
            calendar.set(Calendar.HOUR_OF_DAY, hour) // 使用 24 小时制
            calendar.set(Calendar.MINUTE, min)
            // 3. 可选：重置秒和毫秒为 0（避免残留值）
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (playDay != 0) {
                calendar.timeInMillis += playDay * 24 * 60 * 60 * 1000
            }
            return calendar
        }

        fun timeYMHMS(ts:Long) : String{
            return timeYMHMS(Calendar.getInstance().also { it.timeInMillis = ts })
        }

        fun timeYMHMS(calendar: Calendar) : String{
            // 获取年、月、日、时、分、秒
            val year: Int = calendar.get(Calendar.YEAR)
            val month: Int = calendar.get(Calendar.MONTH) + 1 // 月份从 0 开始，需要 +1
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val minute: Int = calendar.get(Calendar.MINUTE)
            val second: Int = calendar.get(Calendar.SECOND)
            // 格式化输出
            return String.format(
                Locale.CHINA,
                "%04d-%02d-%02d %02d:%02d:%02d",  // 格式：YYYY-MM-DD HH:MM:SS
                year, month, day, hour, minute, second
            )
        }

        fun timeDayAndTimeStrs(calendar: Calendar) : Pair<String, String>{
            // 获取年、月、日、时、分、秒
            val year: Int = calendar.get(Calendar.YEAR)
            val month: Int = calendar.get(Calendar.MONTH) + 1 // 月份从 0 开始，需要 +1
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val minute: Int = calendar.get(Calendar.MINUTE)
            // 格式化输出
            return String.format(
                Locale.CHINA,
                "%04d-%02d-%02d",  // 格式：YYYY-MM-DD
                year, month, day
            ) to String.format(
                Locale.CHINA,
                "%02d:%02d",  // 格式：HH:MM
                hour, minute
            )
        }

        fun targetTsToNextDayCalendar(ts: Long) : Calendar{
            val c = Calendar.getInstance()
            c.timeInMillis = ts + 24 * 60 * 60 * 1000
            return c
        }
    }
}