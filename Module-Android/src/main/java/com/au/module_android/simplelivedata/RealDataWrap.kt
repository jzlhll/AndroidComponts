package com.au.module_android.simplelivedata

/**
 * @author au
 * Date: 2023/2/17
 * Description 将真实类型T data，包装在被包装的数据类型中。
 */
data class RealDataWrap<T:Any>(@Status var status:Int = Status.NONE, //状态
                           var data:T? = null,  //真实内容
                           var message:String? = null, //额外信息String
                           var code:Int? = null  //额外信息Int
)