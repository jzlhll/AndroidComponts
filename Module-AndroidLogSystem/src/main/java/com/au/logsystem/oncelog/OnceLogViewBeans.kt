package com.au.logsystem.oncelog

import com.au.module_android.utils.FileLog

data class LogViewShownInfo(var isWrap: Boolean, var fullScreen: Boolean, var time: Boolean,
                            var threadProcess: Boolean, var level: Boolean, var tag: Boolean)
{
    fun isShouldShowOriginal() : Boolean {
        return fullScreen && time && threadProcess && level && tag
    }
}

/**
 * showBits
 * [0] -> time
 * [1] -> threadProcess
 * [2] -> level
 * [3] -> tag
 */
data class LogViewNormalBean(val index:Int,
                             val orig:String,
                             val info: FileLog.LogParserInfo?,
                             var showBits: LogViewShownInfo) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogViewNormalBean

        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }
}