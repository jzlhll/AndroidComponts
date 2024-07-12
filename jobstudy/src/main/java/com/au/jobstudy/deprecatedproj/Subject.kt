package com.au.jobstudy.deprecatedproj

import com.au.jobstudy.R
import com.au.jobstudy.deprecatedproj.DataItem.MediaItem
import com.au.module_android.utils.unsafeLazy
import java.lang.Integer.max

fun fillDataItemMode(dataItem: DataItem, mode: CheckupDescMode) {
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
            CheckupDescMode("阅读某个单元课文。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 600))),
            CheckupDescMode("叫爸爸妈妈听写2课生字。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
            CheckupDescMode("抄写一课生字，字写2遍，组词写一遍。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
            CheckupDescMode("读生字表3课，并每个生字口头组词。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 600))),
        ))

    object SelfRead : Subject("课外阅读", 6, 7,
        actions = arrayOf(
            CheckupDescMode("美文赏析，读2篇课外作文，每篇读2遍。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 600))),
        ))
    object English : Subject("英语拼写", 8, 13,
        actions = arrayOf(
        CheckupDescMode("从英语书本上找8个单词，每个抄2遍。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
            CheckupDescMode("叫爸爸妈妈听写单词5-8个。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 1, 3))),
    ))
    object Maths : Subject("数学", 14, 21,
        actions = arrayOf(
        CheckupDescMode("做1页黄冈卷，并自己用红笔批改，再让爸爸妈妈检查。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
        CheckupDescMode("做2页黄冈卷，交给爸爸妈妈再批改。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
        CheckupDescMode("找做错的知训或试卷，2题，在草稿本画图解答。不懂的，找爸爸妈妈举一反三。", arrayOf(CheckupMode(MediaItem.TYPE_PIC, 2, 4))),
    ))

    object Science : Subject("科学", 22, 23,
        actions = arrayOf(
            CheckupDescMode("找2-3个单元阅读。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 600))),
            CheckupDescMode("阅读老师总结的打印内容。", arrayOf(CheckupMode(MediaItem.TYPE_VOICE, 120, 600))),
        ))
    //todo 最大值
}

private val allSubjects by unsafeLazy {
    listOf(Subject.Chinese, Subject.English, Subject.Maths, Subject.Science, Subject.SelfRead)
}

private val maxRandoms by unsafeLazy {
    var max = -1
    allSubjects.forEach {
        max = max(it.randomEnd, max)
    }
    max + 1
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
    val random = (Math.random() * maxRandoms).toInt()
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
    while (second == first) {
        second = randomGetSubject()
    }

    return arrayOf(first, second)
}

fun subjectToColorId(subject:String):Int {
    return when (nameToSubject(subject)) {
        Subject.Chinese -> R.color.color_chinese
        Subject.Science -> R.color.color_science
        Subject.SelfRead -> R.color.color_self_read
        Subject.Maths -> R.color.color_math
        Subject.English -> R.color.color_english
    }
}