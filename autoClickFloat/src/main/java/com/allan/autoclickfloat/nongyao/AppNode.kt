package com.allan.autoclickfloat.nongyao

import androidx.annotation.CallSuper
import com.au.module_android.Apps
import com.au.module_android.utils.openApp

abstract class AppNode(val pkg:String,
                       val progress:Int,
                       val nextNode:AppNode?) {
    protected var isLive = true

    open fun startNewApp(pkg:String) : Boolean{
        return openApp(Apps.app, pkg)
    }

    abstract suspend fun action() : Boolean

    @CallSuper
    open fun stop() {
        isLive = false
    }

    /**
     * 强行转换。报错自行负责。
     */
    inline fun <reified T:AppNode> nextNodeAs() : T{
        return nextNode as T
    }
}