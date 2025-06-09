package com.au.module_okhttp.exceptions

import androidx.annotation.Keep

/**
 * 时间戳纠正异常
 * @param hasTimestampInfo 是否有内容，如果false就不能重试了。
 */
@Keep
class AuTimestampErrorException(val timestampOffset:Long, val hasTimestampInfo:Boolean, msg:String) : RuntimeException(msg)