package com.au.module_android.utils

import android.content.res.AssetManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class UnzipHelper {
    private val BYTE_SIZE = 2048
    private val notSupportDir = "../"

    /**
     * 添加名字，即可变成目录
     */
    private fun String.filename(name:String) : String {
        if (this.endsWith("/") || this.endsWith("\\")) {
            return this + name
        }
        return this + File.separator + name
    }

    fun deleteDirectory(directory: String) {
        deleteDirectory(File(directory))
    }

    fun deleteDirectory(directory: File?) {
        val files = directory?.listFiles()
        if (files != null) {
            for (subFile in files) {
                if (subFile.isDirectory) {
                    deleteDirectory(subFile)
                } else {
                    subFile.delete()
                }
            }
        }
        directory?.delete()
    }

    /**
     * 构建目录
     * @param outputDir
     * @param subDir
     */
    private fun createDirectory(outputDir: String, subDir:String? = null) {
        val file = if (subDir == null || "" == subDir.trim()) { //子目录不为空
            File(outputDir)
        } else {
            File(outputDir.filename(subDir))
        }

        if (!file.exists()) {
            file.parentFile?.let {
                if (it.exists()) {
                    it.mkdirs()
                }
            }

            file.mkdirs()
        }
    }

    fun unZip(file: File?, outputDir: String):Boolean {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(file) //charset utf-8

            createDirectory(outputDir) //创建输出目录
            val enums: Enumeration<*> = zipFile.entries()
            while (enums.hasMoreElements()) {
                val entry = enums.nextElement() as ZipEntry
                if (entry.name.contains(notSupportDir)) {
                    continue
                }

                if (entry.isDirectory) { //是目录
                    createDirectory(outputDir, entry.name) //创建空目录
                } else { //是文件
                    val tmpFile = File(outputDir.filename(entry.name))
                    createDirectory(tmpFile.parent!!, null) //创建输出目录
                    try {
                        zipFile.getInputStream(entry).use { input ->
                            FileOutputStream(tmpFile).use { out ->
                                var length: Int
                                val b = ByteArray(BYTE_SIZE)
                                while (input.read(b).also { length = it } != -1) {
                                    out.write(b, 0, length)
                                }
                            }
                        }
                    } catch (ex: IOException) {
                        return false
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e:NullPointerException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                zipFile?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        return true
    }

    fun unZipFromAsset(assetMgr: AssetManager, assetName: String, outputDirectory: String, isReWrite: Boolean = true) {
        var file = File(outputDirectory)
        if (!file.exists()) {
            file.mkdirs()
        }

        val inputStream = assetMgr.open(assetName)
        val zipInputStream = ZipInputStream(inputStream)

        var zipEntry = zipInputStream.nextEntry
        val buffer = ByteArray(4096)
        var count:Int
        while (zipEntry != null) {
            val entryName = zipEntry.name
            if (entryName.contains(notSupportDir)) {
                //do next and continue
            } else if (zipEntry.isDirectory) {
                file = File(outputDirectory.filename(zipEntry.name))
                if (isReWrite || !file.exists()) {
                    file.mkdir()
                }
            } else {
                file = File(outputDirectory.filename(zipEntry.name))

                if (isReWrite || !file.exists()) {
                    file.createNewFile()
                    val fileOutputStream = FileOutputStream(file)
                    while (zipInputStream.read(buffer).also { count = it } > 0) {
                        fileOutputStream.write(buffer, 0, count)
                    }
                    fileOutputStream.close()
                }
            }

            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    fun copyFromAssets(assetManager: AssetManager, assetPath:String = "", assetNames: Array<String>, targetDir:String, rewrite:Boolean = true) {
        // copy files form assets folder to files
        try {
            val dir = File(targetDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val list = assetManager.list(assetPath)
            if (!list.isNullOrEmpty()) {
                for (item in list) {
                    if (assetNames.contains(item)) {
                        var shouldCopy = false

                        val file = File(targetDir, item)
                        if (file.exists()) {
                            if (rewrite) {
                                file.delete()
                                shouldCopy = true
                            }
                        } else {
                            shouldCopy = true
                        }

                        if (shouldCopy) {
                            val fileOutputStream = FileOutputStream(file)
                            val bufferedInputStream = BufferedInputStream(assetManager.open("$assetPath/$item"))
                            var len: Int
                            val buf = ByteArray(2048)
                            while (bufferedInputStream.read(buf).also { len = it } > 0) {
                                fileOutputStream.write(buf, 0, len)
                            }
                            fileOutputStream.close()
                            bufferedInputStream.close()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}