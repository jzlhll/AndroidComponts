package com.allan.autoclickfloat.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 实体类
 */
@Keep
@Entity
data class Step(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val locX:Int,
    val locY:Int,
    val rotation:Int,
    val stepIndex:Int,
    val projectId:Int
)