package com.au.jobstudy.check.api

import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.modes.CheckMode
import com.au.jobstudy.check.modes.MediaType

class SummerGeneratorApi : AbsGeneratorApi() {
    override fun createADayWorkEntities(weekStartDay: Int, day: Int): List<WorkEntity> {
        val yuwen = WorkEntity("语文", "练字2页。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照1~3张上传。",3, 1)),
            day, weekStartDay, false,
            "#FF9A00", false, 0)

        val shuxue = WorkEntity("数学", "口算练习册4页。",
            listOf(CheckMode(MediaType.TYPE_PARENT, "叫家长扫码批改后，家长密码确认。")),
            day, weekStartDay, false,
            "#1111FF", false, 0)

        val english = WorkEntity("英语", "抄写8个单词，每个单词4-5遍，中文意思一遍。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照上传。",2, 1)),
            day, weekStartDay, false,
            "#ff77aa", false, 0)

        val english2 = WorkEntity("英语", "小学英语写字，两页。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照上传。",1, 1)),
            day, weekStartDay, false,
            "#ff77aa", false, 0)

        val song = WorkEntity("背诗", "从小书上，学习并背诵一首新诗。",
            listOf(CheckMode(MediaType.TYPE_VIDEO, "拍摄视频上传。", 60, 10)),
            day, weekStartDay, false,
                "#5ddd11", false, 0)

        val read = WorkEntity("阅读", "课外阅读20分钟。",
            listOf(CheckMode(MediaType.TYPE_BELIEVE, "相信你是乖孩子，已经完成阅读任务，点击确认完成吧。", 0, 0)),
            day, weekStartDay, false,
            "#8811ff", false, 0)

        val play = WorkEntity("运动", "运动1个半小时。",
            listOf(CheckMode(MediaType.TYPE_BELIEVE, "相信你是乖孩子，已经完成运动目标，点击确认完成吧。", 0, 0)),
            day, weekStartDay, false,
            "#ddd8da", false, 0)

        return listOf(yuwen, shuxue, english, english2, song, read, play)
    }

    override fun createWeekWorkEntities(weekStartDay: Int): List<WorkEntity> {
        val shuxueWeek = WorkEntity("数学", "学习一个新单元。",
            listOf(CheckMode(MediaType.TYPE_PARENT, "家长检查并确认。")),
            0, weekStartDay, true,
            "#047fff", false, 0)

        val shuxueWeek2 = WorkEntity("数学", "完成一张新单元的试卷。",
            listOf(CheckMode(MediaType.TYPE_PARENT, "家长检查并确认。")),
            0, weekStartDay, true,
            "#11aaff", false, 0)

        val read = WorkEntity("阅读", "每周至少摘抄2篇，拍下读书笔记。",
            listOf(CheckMode(MediaType.TYPE_PIC, "拍照上传", 7, 2)),
            0, weekStartDay, true,
            "#ff0011", true, 0)

        return listOf(shuxueWeek2, shuxueWeek, read)
    }

}