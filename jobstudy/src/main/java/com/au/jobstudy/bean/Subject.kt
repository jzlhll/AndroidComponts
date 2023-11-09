package com.au.jobstudy.bean

import java.lang.RuntimeException

enum class Subject {
    Chinese,
    EnglishWrite,
    Read,
    Science,
    Maths,
    Every53Maths,
    Every53Chinese,
    Every53English,
}

private const val Chinese = "语文"
private const val EnglishWrite = "英语拼写"
private const val Read = "课外阅读"
private const val Science = "科学"
private const val Maths = "数学"
private const val Every53Maths = "53天天练-数学"
private const val Every53Chinese = "53天天练-语文"
private const val Every53English = "53天天练-英语"

fun Subject.subjectToName():String {
    return when (this) {
        Subject.Chinese-> Chinese
        Subject.EnglishWrite->EnglishWrite
        Subject.Read->Read
        Subject.Science->Science
        Subject.Maths->Maths
        Subject.Every53Maths->Every53Maths
        Subject.Every53Chinese->Every53Chinese
        Subject.Every53English->Every53English
    }
}

internal fun String.nameToSubject():Subject {
    return when (this) {
        Chinese ->Subject.Chinese
        EnglishWrite->Subject.EnglishWrite
        Read->Subject.Read
        Science->Subject.Science
        Maths->Subject.Maths
        Every53Maths->Subject.Every53Maths
        Every53Chinese->Subject.Every53Chinese
        Every53English->Subject.Every53English
        else -> throw RuntimeException("no this.")
    }
}

//概率出现题目
private val percentMap = hashMapOf(
    Subject.Chinese to 5,
    Subject.EnglishWrite to 5,
    Subject.Read to 10,
    Subject.Science to 20,
    Subject.Maths to 10,
    Subject.Every53Maths to 20,
    Subject.Every53Chinese to 10,
    Subject.Every53English to 10
)

fun Subject.percent():Int {
    var allPercent = 0
    percentMap.forEach { (_, percent) -> allPercent += percent }
    if (allPercent != 100) {
        throw RuntimeException("各个科目的总概率设置错误！")
    }

    return percentMap[this]!!
}