package com.allan.autoclickfloat.database

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Keep
@Dao
interface StepDao {
    @Query("SELECT * FROM Step WHERE projectId = :projectId")
    fun getAll(projectId:Int) : List<Step>

    @Insert
    fun insert(project: Step)

    @Delete
    fun delete(project: Step)

    @Update
    fun update(project: Step)
}