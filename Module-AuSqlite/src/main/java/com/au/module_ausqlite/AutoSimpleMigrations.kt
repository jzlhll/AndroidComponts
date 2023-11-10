package com.au.module_ausqlite

import android.database.sqlite.SQLiteDatabase

/**
 * @author allan.jiang
 * @date :2023/11/10 17:15
 * @description: 比如某一个EntityTable类，变动了，则改进versionCode升级。
 * 则会自动进行升级操作。采用变更临时表的方式来处理。
 *
 * 3. 如果不传入某个Class到AuSqliteHelper.init，则认为移除了。drop掉。
 * 4. 如果同一个字段名字相同，但是类型变了。我将不会给你迁移。除非你自己编写了对于某张表的迁移代码。
 */
class AutoSimpleMigrations {
    fun onVersionChange(db: SQLiteDatabase) {
        val oldTabs = db.allTableNames()
        val newTabs = AuSqliteHelper.creator?.tableNames

        //1. 一张表都不需要了。全部删掉。
        if (newTabs == null) {
            oldTabs.forEach {
                db.execSQL(sqlDeleteTable(it))
            }
            return
        }

        //2. 对比
        for (oldTab in oldTabs) {
            if (newTabs.contains(oldTab)) {
                //2.1 新表包含了老表，则通过temp迁移一下。
                //later: 由于我们要求，只要发生变化，每次都是重新创建表。
                val tableName = "your_table_name"
                val sql: String = db.getDatabaseInfo().getCreateStatement(tableName)

            } else {
                //2.2 新表不包含老表，则删除老表，新建新表。
                db.execSQL(sqlDeleteTable(oldTab))
            }
        }
    }
}