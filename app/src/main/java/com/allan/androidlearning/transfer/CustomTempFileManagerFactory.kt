package com.allan.androidlearning.transfer

import com.au.module_android.Globals
import fi.iki.elonen.NanoHTTPD.DefaultTempFile
import fi.iki.elonen.NanoHTTPD.TempFile
import fi.iki.elonen.NanoHTTPD.TempFileManager
import fi.iki.elonen.NanoHTTPD.TempFileManagerFactory
import java.io.File

class CustomTempFileManagerFactory : TempFileManagerFactory {
    override fun create() = MyDefaultTempFileManager()
}

class MyDefaultTempFileManager : TempFileManager {
    private val tmpdir: File

    private val tempFiles: MutableList<TempFile>

    init {
        val tmpDirStr = Globals.goodCacheDir.absolutePath + "/nanoTmp"
        this.tmpdir = File(tmpDirStr)
        if (!tmpdir.exists()) {
            tmpdir.mkdirs()
        }
        this.tempFiles = ArrayList<TempFile>()
    }

    override fun clear() {
        for (file in this.tempFiles) {
            try {
                file.delete()
            } catch (_: Exception) {
            }
        }
        this.tempFiles.clear()
    }

    @Throws(Exception::class)
    override fun createTempFile(filename_hint: String?): TempFile {
        val tempFile = DefaultTempFile(this.tmpdir)
        this.tempFiles.add(tempFile)
        return tempFile
    }
}
