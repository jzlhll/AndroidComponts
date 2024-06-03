package com.allan.autoclickfloat.nongyao

import com.au.module_android.Apps
import com.au.module_android.utils.openApp

abstract class AppNode(val pkg:String,
                       val progress:Int,
                       val nextNode:AppNode?) {
    open fun startNewApp(pkg:String) : Boolean{
        return openApp(Apps.app, pkg)
    }

    abstract suspend fun action() : Boolean

    /**
     * 强行转换。报错自行负责。
     */
    inline fun <reified T:AppNode> nextNodeAs() : T{
        return nextNode as T
    }
}