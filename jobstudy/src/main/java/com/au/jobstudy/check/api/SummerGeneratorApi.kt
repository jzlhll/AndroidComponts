package com.au.jobstudy.check.api

import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.modes.CheckMode
import com.au.jobstudy.check.modes.MediaType

class SummerGeneratorApi : AbsGeneratorApi() {
    override fun createADayWorkEntities(weekStartDay: Int, day: String): List<WorkEntity> {
        val yuwen = WorkEntity("语文", "练字2页。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照1~3张上传。",1, 3)),
            day.toInt(), weekStartDay, false,
            "#FF9A00")

        val shuxue = WorkEntity("数学", "口算练习册4页。",
            listOf(CheckMode(MediaType.TYPE_PIC, "扫码批改后，拍照2~4张上传。",2, 4),
                CheckMode(MediaType.TYPE_MANUAL, "家长确认。")),
            day.toInt(), weekStartDay, false,
            "#1111FF")

        val english = WorkEntity("英语", "抄写8个单词，每个单词4-5遍，中文意思一遍。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照上传。",1, 2)),
            day.toInt(), weekStartDay, false,
            "#ff77aa")

        val song = WorkEntity("背诗", "从小书上，学习与背诵一首新诗。",
            listOf(CheckMode(MediaType.TYPE_VOICE, "录音上传。", 10, 60)),
                day.toInt(), weekStartDay, false,
                "#5ddd11")

        return listOf(yuwen, shuxue, english, song)
    }

    override fun createWeekWorkEntities(weekStartDay: Int): List<WorkEntity> {
        val shuxueWeek = WorkEntity("数学", "学习一个新单元。",
            listOf(CheckMode(MediaType.TYPE_MANUAL, "家长检查并确认。")),
            0, weekStartDay, true,
            "#047fff")

        val shuxueWeek2 = WorkEntity("数学", "完成一张新单元的试卷。",
            listOf(CheckMode(MediaType.TYPE_MANUAL, "家长检查并确认。")),
            0, weekStartDay, true,
            "#11aaff")

        val read = WorkEntity("阅读", "拍下读书笔记。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照上传", 1, 2)),
            0, weekStartDay, true,
            "#ff0011")

        return listOf(shuxueWeek2, shuxueWeek, read)
    }

}