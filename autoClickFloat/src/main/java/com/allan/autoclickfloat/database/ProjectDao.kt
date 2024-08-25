package com.allan.autoclickfloat.database

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Keep
@Dao
interface ProjectDao {
    @Query("SELECT * FROM Project")
    fun getAll() : List<Project>

    @Insert
    fun insert(project: Project)

    @Delete
    fun delete(project: Project)

    @Update
    fun update(project: Project)
}