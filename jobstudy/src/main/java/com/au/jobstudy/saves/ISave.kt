package com.au.jobstudy.saves

import com.au.jobstudy.bean.DataItem

/**
 * @author allan.jiang
 * @date :2023/11/9 18:11
 */
interface ISave {
    suspend fun loadWeekData(anyDay:String, notExistGenerate: Boolean) : List<DataItem>

    fun isLoadedWeek(anyDay:String) : Boolean
    /**
     * 查询。可为空。一周以内的任意一天，都会查询到整个一周
     */
    fun getWeekData(anyDay:String) : List<DataItem>?

    /**
     * 更新一天的某一个index的结果。
     */
    suspend fun updateOneDay(item: DataItem):Boolean

    /**
     * 删除long转的某一天的记录
     */
    suspend fun deleteOneDay(day:String):Boolean

    /**
     * 删除一周的数据。一周以内的任意一天，都会删除。
     */
    suspend fun deleteOneWeek(anyDay:String):Boolean

    /**
     *  从一周的数据里面抽取，生成当天的数据。
     * */
    fun getDay(day:String) : List<DataItem>?
}

/**
 * @author allan.jiang
 * @date :2023/11/9 18:11
 */
interface ISaveViewModel {
    /**
     * 查询。可为空。一周以内的任意一天，都会查询到整个一周
     */
    fun getWeekData(day:String, notExistGenerate: Boolean, mask:String? = null)

    /**
     * 更新一天的某一个index的结果。
     */
    fun updateOneDay(item: DataItem, mask:String? = null)

    /**
     * 删除long转的某一天的记录
     */
    fun deleteOneDay(day:String, mask:String? = null)

    /**
     * 删除一周的数据。一周以内的任意一天，都会删除。
     */
    fun deleteOneWeek(day:String, mask:String? = null)

    /**
     *  从一周的数据里面抽取，生成当天的数据。
     * */
    fun getDay(day:String, mask:String? = null)
}