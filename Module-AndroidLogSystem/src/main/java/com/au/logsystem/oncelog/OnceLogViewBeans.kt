package com.au.logsystem.oncelog

import com.au.module_android.utils.FileLog

fun generateShowBits(time: Boolean, threadProcess: Boolean, level: Boolean, tag: Boolean) : BooleanArray{
    return booleanArrayOf(time, threadProcess, level, tag)
}

/**
 * showBits
 * [0] -> time
 * [1] -> threadProcess
 * [2] -> level
 * [3] -> tag
 */
data class LogViewNormalBean(val index:Int, val orig:String, val info: FileLog.LogParserInfo?,
                             val showBits: BooleanArray,
                             var isUiWrapText: Boolean) {
    fun showTime() = showBits[0]
    fun showThreadProcess() = showBits[1]
    fun showLevel() = showBits[2]
    fun showTag() = showBits[3]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogViewNormalBean

        if (index != other.index) return false
        if (orig != other.orig) return false
        if (info != other.info) return false
        if (!showBits.contentEquals(other.showBits)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + orig.hashCode()
        result = 31 * result + (info?.hashCode() ?: 0)
        result = 31 * result + showBits.contentHashCode()
        return result
    }
}