package com.au.logsystem

import com.au.module_android.utils.logdNoFile
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class OnceLogViewReader(private val file: File) {

    /**
     * 读取文件，跳过指定行数，读取指定行数，返回读取到的行数和是否文件提前结束，提前结束为true。
     */
    fun readAll(): List<String> {
        // 2. 读取行
        val lines = mutableListOf<String>()
        BufferedReader(FileReader(file)).use {
            while (true) {
                val line = it.readLine()
                if (line == null) {
                    break
                }
                lines.add(line)
            }

            return lines
        }
    }

}