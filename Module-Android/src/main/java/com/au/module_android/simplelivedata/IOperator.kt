package com.au.module_android.simplelivedata

/**
 * @author au
 * Date: 2023/2/17
 * Description 普通的liveData的设置
 */
interface IOperator<T> {
    fun success(data:T? = null, code:Int? = null, msg:String?=null)
    fun error(data:T? = null, code:Int? = null, msg:String?=null)
    fun running(data:T? = null, code:Int? = null, msg:String?=null)
}