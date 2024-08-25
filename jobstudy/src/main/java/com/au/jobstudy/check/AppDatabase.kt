package com.au.jobstudy.check

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.StarEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.dao.CompletedDao
import com.au.jobstudy.check.dao.StarDao
import com.au.jobstudy.check.dao.WorkDao
import com.au.module_android.Globals

@Database(entities = [WorkEntity::class, CompletedEntity::class, StarEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var instance:AppDatabase? = null
        private const val DATA_BASE_NAME = "job_study"
        private val lock = Any()

        /**
         * 调用单例数据库
         */
        val db : AppDatabase
            get() {
            if (instance == null) {
                synchronized(lock) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            Globals.app,
                            AppDatabase::class.java,
                            DATA_BASE_NAME
                        )
                            //.enableMultiInstanceInvalidation() 多进程启用
                            .build()
                    }
                }
            }
            return instance!!
        }
    }

    abstract fun getWorkDao(): WorkDao
    abstract fun getCompletedDao(): CompletedDao
    abstract fun getStarDao(): StarDao
}