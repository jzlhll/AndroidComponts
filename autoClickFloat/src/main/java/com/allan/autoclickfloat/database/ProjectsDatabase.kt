package com.allan.autoclickfloat.database

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.au.module_android.Globals

@Keep
@Database(entities = [Project::class, Step::class], version = 1, exportSchema = false)
abstract class ProjectsDatabase : RoomDatabase() {
    companion object {
        private val _dbLock = Any()
        private var _db:ProjectsDatabase? = null
        /**
         * 数据库
         */
        val db:ProjectsDatabase
            get() {
                if (_db == null) {
                    synchronized(_dbLock) {
                        if (_db == null) {
                            _db = Room.databaseBuilder(
                                Globals.app, ProjectsDatabase::class.java, "record_projects"
                            ).build()
                        }
                    }
                }
                return _db!!
            }
    }

    abstract fun projectDao() : ProjectDao
    abstract fun stepDao() : StepDao
}