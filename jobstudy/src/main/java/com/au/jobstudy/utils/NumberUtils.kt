package com.au.jobstudy.utils

/**
 * 秒数int值，转成00:00:00格式
 */
fun secondToTimeHMS(second: Long): String {
    return if (second >= 0) {
        val timeStr = "${second/3600}:${second%3600/60}:${second % 60}"
        String.format(timeStr, second/3600, second % 3600 / 60, second % 60)
    } else {
        "00:00:00"
    }
}

/**
 * 秒数int值，转成00:00。适用于比较短时间。超过1小时会错误的。
 */
fun secondToTimeMS(second: Int): String {
    return if (second >= 0) {
        val timeStr = "${second%3600/60}:${second % 60}"
        String.format(timeStr, second % 3600 / 60, second % 60)
    } else {
        "00:00"
    }
}