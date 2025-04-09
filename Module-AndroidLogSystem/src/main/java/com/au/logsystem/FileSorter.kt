package com.au.logsystem

import java.io.File
import java.util.Calendar

class FileSorter {
    companion object {
        fun groupToName(group: Group): String {
            return when (group) {
                Group.TODAY -> "今天"
                Group.YESTERDAY -> "昨天"
                Group.WITHIN_WEEK -> "一周内"
                Group.OLDER -> "以前"
            }
        }
    }

    // 分组枚举
    enum class Group {
        TODAY, YESTERDAY, WITHIN_WEEK, OLDER
    }

    // 非静态方法
    fun groupFiles(files: MutableSet<File>): MutableMap<Group, MutableList<File>> {
        // 获取当前时间戳
        val now = System.currentTimeMillis()
        val nowCalendar = Calendar.getInstance()
        nowCalendar.setTimeInMillis(now)

        // 解析文件名，获取时间戳，并缓存
        val fileTimestampMap: MutableMap<File, Long> = HashMap()
        for (file in files) {
            fileTimestampMap.put(file, parseFileName(file))
        }

        // 按时间戳倒序排序
        val sortedFiles: MutableList<File> = ArrayList(files)
        sortedFiles.sortBy { fileTimestampMap[it] }

        // 初始化分组
        val result: MutableMap<Group, MutableList<File>> = LinkedHashMap()
        for (group in Group.entries) {
            result.put(group, ArrayList())
        }

        // 分组逻辑
        for (file in sortedFiles) {
            val fileTimestamp: Long = fileTimestampMap[file]!!
            val fileCalendar = Calendar.getInstance()
            fileCalendar.setTimeInMillis(fileTimestamp)

            // 判断是否属于今天
            if (isSameDay(nowCalendar, fileCalendar)) {
                result[Group.TODAY]!!.add(file)
            } else if (isYesterday(nowCalendar, fileCalendar)) {
                result[Group.YESTERDAY]!!.add(file)
            } else if (isWithinWeek(nowCalendar, fileCalendar)) {
                result[Group.WITHIN_WEEK]!!.add(file)
            } else {
                result[Group.OLDER]!!.add(file)
            }
        }

        //将result删除空的List对应的key
        val needDeleteKeys = mutableListOf<Group>()
        for (group in result.keys) {
            if (result[group]!!.isEmpty()) {
                needDeleteKeys.add(group)
            }
        }
        needDeleteKeys.forEach { result.remove(it) }
        return result
    }

    // 判断两个时间是否属于同一天
    private fun isSameDay(now: Calendar, file: Calendar): Boolean {
        return now.get(Calendar.YEAR) == file.get(Calendar.YEAR) && now.get(Calendar.MONTH) == file.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == file.get(Calendar.DAY_OF_MONTH)
    }

    // 判断文件时间是否属于昨天
    private fun isYesterday(now: Calendar, file: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_MONTH, -1) // 当前时间减去一天
        return isSameDay(yesterday, file)
    }

    // 判断文件时间是否属于一周内
    private fun isWithinWeek(now: Calendar, file: Calendar): Boolean {
        val oneWeekAgo = now.clone() as Calendar
        oneWeekAgo.add(Calendar.DAY_OF_MONTH, -7) // 当前时间减去七天
        return file.after(oneWeekAgo) && file.before(now)
    }

    // 解析文件名，获取时间戳
    private fun parseFileName(file: File): Long {
        val fileName = file.name
        val lastModified = file.lastModified()
        val fileYear = Calendar.getInstance().apply {
            timeInMillis = lastModified
        }.get(Calendar.YEAR)

        val dateStr = fileName.substringBefore(".") // 更安全的截取方式

        // 支持的分隔符（可根据需求扩展）
        val delimiters = setOf("-", "_")
        val delimiter = delimiters.firstOrNull { dateStr.contains(it) } ?: run {
            throw IllegalArgumentException("文件名中缺少有效的日期分隔符：$dateStr")
        }

        val parts = dateStr.split(delimiter).filter { it.isNotEmpty() }

        // 格式校验（支持 年月日 或 月日 或 年月日时）
        when {
            parts.size !in 2..4 -> throw IllegalArgumentException(
                "日期格式不正确，应为[年-月-日]、[月-日]或[年-月-日-时]，实际：$dateStr"
            )
            parts.any { !it.matches("\\d+".toRegex()) } -> throw IllegalArgumentException(
                "日期部分必须为数字：$dateStr"
            )
        }

        // 智能补全年份（如果缺少）
        val (year, month, day, hour) = when (parts.size) {
            // 格式：月-日（自动补全当前年）
            2 -> {
                arrayOf(fileYear, parts[0].toInt(), parts[1].toInt(), 0)
            }
            // 格式：年-月-日
            3 -> arrayOf(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), 0)
            // 格式：年-月-日-时 或 月-日-时（自动补全年）
            4 -> {
                if (parts[0].length == 4) { // 通过长度判断是否是年份
                    arrayOf(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
                } else {
                    arrayOf(fileYear, parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                }
            }
            else -> throw AssertionError("不可能到达的分支")
        }

        // 日期有效性校验
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // 月份转为0-based
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }
}
