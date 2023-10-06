package com.au.module_android.simplelivedata

import androidx.annotation.IntDef

/**
 * @author allan.jiang
 * Date: 2023/2/17
 * Description 将真实类型T data，包装在被包装的数据类型中。
 */
data class StatusData<T>(@Status var status:Int = Status.NONE, //状态
                         var data:T? = null,  //真实内容
                         var message:String? = null, //额外信息String
                         var code:Int? = null  //额外信息Int
)

/**
 * @author allan.jiang
 * Date: 2023/2/15
 * Description 设计运行的模式
 */
@IntDef(Status.RUNNING, Status.OVER_SUCCESS, Status.OVER_ERROR, Status.NONE)
@Retention(AnnotationRetention.BINARY)
annotation class Status {
    companion object {
        const val NONE = -10 //没有运行，空数据。
        const val RUNNING = 1 //开始了。
        const val OVER_SUCCESS = 0 //结束：成功。
        const val OVER_ERROR = -1 //结束：失败。
    }
}
