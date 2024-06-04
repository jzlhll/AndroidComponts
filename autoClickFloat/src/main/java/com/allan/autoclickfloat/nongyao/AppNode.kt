package com.allan.autoclickfloat.nongyao

import androidx.annotation.CallSuper
import com.au.module_android.Globals
import com.au.module_android.utils.openApp

abstract class AppNode(val pkg:String,
                       val progress:Int,
                       val nextNode:AppNode?) {
    protected var isLive = true

    fun startNewApp() : Boolean{
        return openApp(Globals.app, pkg)
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