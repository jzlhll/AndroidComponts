package com.au.jobstudy

import com.au.aulitesql.AuLiteSql
import com.au.jobstudy.deprecatedproj.DataItem
import com.au.module_android.init.InitApplication

/**
 * @author au
 * @date :2023/11/14 14:05
 * @description:
 */
class MyInitApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        AuLiteSql.getInstance()
            .setDb("jobStudy", 1)
            .setTableClasses(listOf(DataItem::class.java))
            .useThreadPool()
            .openDataBase(this)
    }
}