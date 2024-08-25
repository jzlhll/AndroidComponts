package com.au.module_android.simplelivedata

/**
 * @author au
 * Date: 2023/2/17
 * Description 普通的liveData的设置
 */
interface IOperator<T:Any> {
    /**
     * 设置为成功
     */
    fun success(data:T? = null, code:Int? = null, msg:String?=null)

    /**
     * 设置为失败
     */
    fun error(data:T? = null, code:Int? = null, msg:String?=null)

    /**
     * 设置为running
     */
    fun running(data:T? = null, code:Int? = null, msg:String?=null)

    /**
     * 设置为running。使用老的data
     */
    fun runningOldData(code:Int? = null, msg:String?=null)
}