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
    fun generateWeekData(weekStartDay:String, weekData: DataOneWeek)

    /**
     * 更新一天的某一个index的结果。
     */
    fun updateOneDay(day:String, index:Int, item: DataItem):Boolean

    /**
     * 删除long转的某一天的记录
     */
    fun deleteOneDay(day:String):Boolean

    /**
     * 删除一周的数据。一周以内的任意一天，都会删除。
     */
    fun deleteOneWeek(week:String):Boolean

    /**
     * 查询。可为空。一周以内的任意一天，都会查询到整个一周
     */
    fun readWeekData(week:String):DataOneWeek?

    /**
     *  从一周的数据里面抽取，生成当天的数据。
     * */
    fun getDay(dayInWeek:Int, weekData:DataOneWeek)

    /** 生成一周的数据
     * 可能是立刻生成的。那么生成以后就要落盘。
     * 后续都是从数据库中提取。
     *
     * @param backWeek 往回倒数的周数。0表示当前这周。
     * */
    fun getWeek(backWeek:Int = 0) : DataOneWeek
}