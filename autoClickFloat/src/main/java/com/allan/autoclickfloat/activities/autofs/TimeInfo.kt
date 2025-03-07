package com.allan.autoclickfloat.activities.autofs

import androidx.annotation.Keep
import com.au.module_android.json.fromJson

@Keep
data class TimeInfo(val timeStr:String, val targetTs:Long) {
    fun toJson() : String = "{\"timeStr\":\"$timeStr\",\"targetTs\":$targetTs}"
}

fun timeInfoFromJson(json:String) : TimeInfo? {
    return json.fromJson<TimeInfo>()
}