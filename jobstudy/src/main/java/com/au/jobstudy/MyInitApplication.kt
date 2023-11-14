package com.au.jobstudy

import com.au.aulitesql.AuLiteSql
import com.au.module_android.init.InitApplication

/**
 * @author allan.jiang
 * @date :2023/11/14 14:05
 * @description:
 */
class MyInitApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        AuLiteSql.initDb("jobStudy", 1)
    }
}