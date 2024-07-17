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
    val day:Int,
    val weekStartDay:Int,
    val weekJob:Boolean,
    val colorStr:String,
    /**
     * 是否可以完成。默认false表示正常可以完成；true一直显示在列表中。
     */
    val notCollapse:Boolean,
    @PrimaryKey(autoGenerate = true) var id : Long)