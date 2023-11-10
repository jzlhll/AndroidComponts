package com.au.module_ausqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AuSqliteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        var DATABASE_VERSION = 1
        var DATABASE_NAME = "au_sqlite.db"

        var creator: TableCreator.Output? = null

        fun init(dbName:String? = null, version:Int = 1, tables:List<Class<out EntityTable>>) {
            DATABASE_NAME = dbName ?: DATABASE_NAME
            DATABASE_VERSION = version
            creator = TableCreator(tables).collect()
        }

        fun migrations() {

        }
    }

    private val mAutoMigrations = AutoSimpleMigrations()

    override fun onCreate(db: SQLiteDatabase) {
        creator?.tableCreatesStrings?.forEach {
            db.execSQL(it)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        mAutoMigrations.onVersionChange(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        mAutoMigrations.onVersionChange(db)
    }
}