package com.au.jobstudy.saves

import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.DataOneWeek

/**
 * @author allan.jiang
 * @date :2023/11/9 18:11
 */
interface ISave {
    /**
     * 传入的是当前时间；会转换为具体的周的周一
     */
    fun saveWeekData(week1:String, weekData: DataOneWeek)

    /**
     * 更新一天的某一个index的结果。
     */
    fun updateOneDayData(day:String, index:Int, item: DataItem):Boolean

    /**
     * 删除long转的某一天的记录
     */
    fun deleteOneDay(day:String):Boolean

    /**
     * 删除一周的数据
     */
    fun deleteOneWeek(day:String):Boolean

    /**
     * 查询。可为空。
     */
    fun readWeek(week1:String):DataOneWeek

    fun readAllWeeks():List<DataOneWeek>
}