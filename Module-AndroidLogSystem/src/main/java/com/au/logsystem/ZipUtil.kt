package com.au.logsystem

import android.annotation.SuppressLint
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt

class ZipUtil {
    companion object {
        /**
         * 将文件大小（字节）转换为 MB，并根据大小动态调整小数位数
         *
         * @param sizeInBytes 文件大小（字节）
         * @return 格式化后的文件大小字符串（MB）
         */
        @SuppressLint("DefaultLocale")
        fun formatSize(sizeInBytes: Long): String {
            return when {
                sizeInBytes < 1024 ->
                    "<1 KB"
                sizeInBytes < 100 * 1024 -> {
                    val kb = (sizeInBytes.toDouble() / 1024.0).roundToInt()
                    "$kb KB"
                }
                sizeInBytes < 10 * 1024 * 1024 -> {
                    val mb = sizeInBytes.toDouble() / (1024.0 * 1024.0)
                    String.format("%.1f MB", mb)
                }
                else -> {
                    val mb = (sizeInBytes.toDouble() / (1024.0 * 1024.0)).roundToInt()
                    "$mb MB"
                }
            }
        }

        /**
         * 压缩文件夹为zip文件
         */
        @Throws(IOException::class)
        private fun compressZip(origFileList: List<File?>, outputPath: String) {
            val outputFile = File(outputPath)

            // 创建ZIP输出流（自动关闭资源）
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).apply {
                setLevel(5) // 设置压缩级别（0-9）
            }.use { zipOut ->
                origFileList.filterNotNull().forEach { file ->
                    if (file.exists() && file.isFile) {
                        // 创建ZIP条目
                        zipOut.putNextEntry(ZipEntry(file.name))

                        // 写入文件内容
                        BufferedInputStream(FileInputStream(file)).use { input ->
                            input.copyTo(zipOut)
                        }

                        // 关闭当前条目
                        zipOut.closeEntry()
                    }
                }
            }
        }

        /**
         * 将文件列表拷贝到新目录，然后压缩该目录并返回压缩文件
         *
         * @param origFileList 原始文件列表
         * @return 压缩后的文件
         * @throws IOException 如果操作过程中发生错误
         */
        @Throws(IOException::class)
        fun compressFilesToZip(origFileList: List<File>, targetZipLogDir: String): File? {
            if (origFileList.isEmpty()) {
                return null
            }

            // 创建或清理目标目录
            val targetDir = File(targetZipLogDir)
            if (targetDir.exists()) {
                // 删除目标目录中的所有文件
                targetDir.listFiles()?.let {
                    for (file in it) {
                        if (file.isFile()) {
                            file.delete()
                        }
                    }
                }
            } else {
                targetDir.mkdirs()
            }

            Thread.sleep(250)

            // 生成 outputPath
            val outputPath = generateOutputPath(targetZipLogDir)

            // 压缩目标目录
            compressZip(origFileList, outputPath)

            return File(outputPath)
        }

        /**
         * 生成 outputPath，规则为 factory_log_yyyyMMdd_HHmmss.zip，放在 newDir 同一级目录下
         *
         * @param targetZipLogDir 目标目录
         * @return 生成的 outputPath
         */
        private fun generateOutputPath(targetZipLogDir: String): String {
            // 获取当前时间，格式化为 yyyyMMdd_HHmmss
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
            val timestamp = dateFormat.format(Date())
            // 生成文件名
            val fileName = "log_$timestamp.zip"
            // 返回完整路径
            return File(targetZipLogDir, fileName).absolutePath
        }

        /**
         * 遍历得到dir下所有的文件。
         */
        fun getAllFilesInDir(files: MutableSet<File>, dir: File) {
            val fs = dir.listFiles()
            if (fs != null) {
                for (f in fs) {
                    if (f.isDirectory()) getAllFilesInDir(files, f)
                    if (f.isFile()) {
                        files.add(f)
                    }
                }
            }
        }
//        /**
//         * 从url中提取出日期路径
//         */
//        fun extractDatePath(url: String): String? {
//            val matcher: Matcher = Pattern.compile("(\\d{4}/\\d{2}/\\d{2}/[a-zA-Z0-9]+\\.?(?:7z|zip))").matcher(url)
//            if (matcher.find()) {
//                return matcher.group(1)
//            }
//            val splits = url.split("/".toRegex())
//            if (splits.isNotEmpty()) {
//                return splits[splits.size - 1]
//            }
//            return null
//        }
    }
}
