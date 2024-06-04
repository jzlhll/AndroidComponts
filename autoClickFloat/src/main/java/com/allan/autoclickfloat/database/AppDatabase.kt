package com.allan.autoclickfloat.database

import android.util.Log
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.Globals

@Keep
@Database(entities = [Project::class, Step::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private val _dbLock = Any()

        @Volatile
        private var _db:AppDatabase? = null
        /**
         * 数据库
         */
        val db:AppDatabase
            get() {
                if (_db == null) {
                    synchronized(_dbLock) {
                        if (_db == null) {
                            _db = Room
                                //inMemoryDatabaseBuilder 改为则创建一个内存数据库（进程销毁后，数据丢失）
                                .databaseBuilder(Globals.app, AppDatabase::class.java, "record_projects")
                                .allowMainThreadQueries() //数据库的操作是否允许在主线程中执行
                                .addCallback(object : Callback() { //数据库创建和打开后的回调，可以重写其中的方法
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        Log.d(Const.TAG, "onCreate: db_student")
                                    }
                                })
                                .fallbackToDestructiveMigration() //数据库升级异常之后的回滚
                            .build()
                        }
                    }
                }
                return _db!!
            }
    }

    abstract fun projectDao() : ProjectDao
    abstract fun stepDao() : StepDao
}