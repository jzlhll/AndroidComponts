package com.au.jobstudy.check.generator

import com.au.jobstudy.check.bean.WorkEntity

class SummerDbGenerator : IDbGenerator() {
    override fun createADayWorkEntity(weekStartDay: Int, day: String): List<WorkEntity> {
        val yuwen = WorkEntity("语文", )
    }

}