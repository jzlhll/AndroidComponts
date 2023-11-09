package com.au.jobstudy.saves

import com.au.jobstudy.bean.DataOneWeek

/**
 * @author allan.jiang
 * @date :2023/11/9 18:11
 * @description:
 */
interface ISave {
    /**
     * 传入的是当前时间；会转换为具体的周的周一
     */
    fun saveWeekData(time:Long, weekData: DataOneWeek) {
        saveWeekData(convertTimeToWeek1(), weekData)
    }

    fun saveWeekData(week1:String, weekData: DataOneWeek)

    /**
     * 将传入的时间，转换为那一周的日期2023-09-11
     */
    fun convertTimeToWeek1() : String{
        return ""
    }

    fun updateOneDayData(time:Long, weekData: DataOneWeek)
}