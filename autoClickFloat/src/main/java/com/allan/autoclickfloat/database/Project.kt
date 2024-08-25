package com.allan.autoclickfloat.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 实体类
 */
@Keep
@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val projectId:Int,
    val projectName:String,
)