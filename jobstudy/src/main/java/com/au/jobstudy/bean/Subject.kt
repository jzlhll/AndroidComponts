package com.au.jobstudy.bean

import com.au.jobstudy.R
import com.au.jobstudy.bean.DataItem.MediaItem
import com.au.module_android.utils.unsafeLazy

fun fillDataItemMode(dataItem: DataItem, mode:CheckupDescMode) {
    dataItem.desc = mode.desc
    for (m in mode.modes) {
        if (m.type == MediaItem.TYPE_VOICE) {
            dataItem.minVoiceSec = m.min
            dataItem.maxVoiceSec = m.max
        }

        if (m.type == MediaItem.TYPE_PIC) {
            dataItem.minPicNum = m.min
            dataItem.maxPicNum = m.max
        }

        if (m.type == MediaItem.TYPE_VIDEO) {
            dataItem.minVideoSec = m.min
            dataItem.maxVideoSec = m.max
        }
    }
}

sealed class Subject(val name:String, val randomStart:Int, val randomEnd:Int, val actions:Array<CheckupDescMode>) {
    fun randomOneAction() : CheckupDescMode {
        val size = actions.size
        return actions[(Math.random() * size).toInt()]
    }

    object Chinese : Subject("语文", 0, 5,
        actions = arrayOf(
            CheckupDescMode("阅读某个单元所有课文", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 200, 400))),
            CheckupDescMode("抄一遍或者叫爸爸妈妈听写任意2课所有生字", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
            CheckupDescMode("读生字表3课，并每个生字口头组词。比如: \"飞，飞，飞机。\"", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 320))),
        ))
    object EnglishWrite : Subject("英语拼写", 6, 10,
        actions = arrayOf(
        CheckupDescMode("从英语书本上找，8个单词，每个抄2遍。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
    ))
    object Science : Subject("科学", 11, 19,
        actions = arrayOf(
        CheckupDescMode("找3个单元阅读。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 220, 480))),
        CheckupDescMode("阅读老师总结的打印内容。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 200, 480))),
    ))
//    object Maths : Subject("数学", 26, 30, arrayOf(
//        CheckupDescMode("读课本3个。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 220, 480))),
//    ))
    object Every53Maths : Subject("53天天练-数学", 20, 40,
        actions = arrayOf(
        CheckupDescMode("做三页并自我检查。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
        CheckupDescMode("做两页并自我检查。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
    ))
    object Every53English : Subject("53天天练-英语", 41, 45,
        actions = arrayOf(
        CheckupDescMode("做一课。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
    ))
    object Every53Chinese : Subject("53天天练-语文", 46, 50,
        actions = arrayOf(
        CheckupDescMode("做一课。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
    ))
}

private val allSubjects by unsafeLazy {
    listOf(Subject.Chinese, Subject.EnglishWrite, Subject.Science,
        Subject.Every53Maths, Subject.Every53Chinese, Subject.Every53English)
}

fun nameToSubject(name:String) : Subject {
    for (subj in allSubjects) {
        if (subj.name == name) {
            return subj
        }
    }
    throw RuntimeException()
}

fun randomGetSubject() : Subject {
    val random = (Math.random() * 51).toInt()
    for (subj in allSubjects) {
        if (random>=subj.randomStart && random <= subj.randomEnd) {
            return subj
        }
    }
    throw RuntimeException()
}

fun randomGetTwoSubjects() : Array<Subject> {
    val first = randomGetSubject()
    var second = randomGetSubject()
    if (second == first) {
        second = randomGetSubject()
    }
    return arrayOf(first, second)
}

fun subjectToColor(subject:String):Int {
    return when (nameToSubject(subject)) {
        Subject.Chinese-> R.color.color_chinese
        Subject.Every53Chinese-> R.color.color_chinese
        Subject.Science-> R.color.color_science
        Subject.Every53Maths-> R.color.color_math
        Subject.EnglishWrite-> R.color.color_english
        Subject.Every53English-> R.color.color_english
    }
}