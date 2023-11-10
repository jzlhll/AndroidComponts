package com.au.module_ausqlite

import android.database.sqlite.SQLiteDatabase
import androidx.annotation.WorkerThread


//常用sql语句大全 或者 代码片段

@WorkerThread
fun SQLiteDatabase.hasTableName(tableName:String) : Boolean{
    val query = "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'"
    val cursor = this.rawQuery(query, null)
    val exists = cursor.moveToFirst()
    cursor.close()
    return exists
}

@WorkerThread
fun SQLiteDatabase.allTableNames(): List<String> {
    val tableNames: MutableList<String> = ArrayList()
    val cursor = this.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            val columnId = cursor.getColumnIndex("name")
            if (columnId >= 0) {
                val tableName = cursor.getString(columnId)
                tableNames.add(tableName)
            }
            cursor.moveToNext()
        }
    }
    cursor.close()
    return tableNames
}

fun sqlDeleteTable(tableName:String) = "DROP TABLE IF EXISTS $tableName"