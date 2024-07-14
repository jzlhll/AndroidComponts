package com.au.jobstudy.check.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.au.jobstudy.check.modes.CheckMode

@Entity(tableName = "work")
@TypeConverters(CheckModesConverter::class)
data class WorkEntity(
    val subject:String,
    val desc:String,
    val checkModes: List<CheckMode>,
    val checkModeDesc: String,
    val day:Int,
    val weekStartDay:Int,
    val weekJob:Boolean,
    @PrimaryKey(autoGenerate = true) val id : Int = 0)