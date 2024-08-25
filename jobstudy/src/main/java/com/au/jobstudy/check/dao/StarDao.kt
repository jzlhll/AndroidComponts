package com.au.jobstudy.check.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.au.jobstudy.check.bean.StarEntity

@Dao
interface StarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: StarEntity)

    @Query("select * from star WHERE name = :name")
    fun queryDingByName(name:String) : List<StarEntity>

    @Query("select * from star WHERE dingDay = :day")
    fun queryDingsByDay(day:Int) : List<StarEntity>

    @Query("select * from star")
    fun queryAll() : List<StarEntity>
}