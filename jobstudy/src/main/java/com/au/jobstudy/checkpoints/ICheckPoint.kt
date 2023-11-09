package com.au.jobstudy.checkpoints

import com.au.jobstudy.bean.DataOneWeek

/**
 * @author allan.jiang
 * @date :2023/11/9 17:32
 * @description:
 */
interface ICheckPoint {
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