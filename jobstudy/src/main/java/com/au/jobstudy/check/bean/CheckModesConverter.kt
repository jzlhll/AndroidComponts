package com.au.jobstudy.check.bean

import androidx.room.TypeConverter
import com.au.jobstudy.check.modes.CheckMode
import com.au.module_android.Globals
import com.au.module_android.json.fromJsonList
import java.util.Collections

class CheckModesConverter {
    @TypeConverter
    fun listCheckModeToString(checkModes: List<CheckMode>) : String {
        return Globals.gson.toJson(checkModes)
    }

    @TypeConverter
    fun stringToListCheckMode(json:String) : List<CheckMode> {
        return json.fromJsonList<CheckMode>() ?: Collections.emptyList()
    }
}