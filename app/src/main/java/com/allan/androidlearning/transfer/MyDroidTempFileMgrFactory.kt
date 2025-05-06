package com.allan.androidlearning.transfer

import com.au.module_android.Globals
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.unsafeLazy
import fi.iki.elonen.NanoHTTPD.DefaultTempFile
import fi.iki.elonen.NanoHTTPD.TempFile
import fi.iki.elonen.NanoHTTPD.TempFileManager
import fi.iki.elonen.NanoHTTPD.TempFileManagerFactory
import java.io.File

class MyDroidTempFileMgrFactory : TempFileManagerFactory {
    override fun create() = MyDroidTempFileManager()
}

class MyDroidTempFileManager : TempFileManager {
    private val tmpdir: File

    private val tempFiles: MutableList<TempFile>

    private val addrTag by unsafeLazy {
        val t = this.toString()
        val index = t.indexOf("@")
        "[" + t.substring(index + 1) + "]"
    }

    init {
        val tmpDirStr = Globals.goodCacheDir.absolutePath + File.separatorChar + TEMP_CACHE_DIR
        this.tmpdir = File(tmpDirStr)
        //logdNoFile { "$addrTag make tmp dir $tmpDirStr" }
        if (!tmpdir.exists()) {
            tmpdir.mkdirs()
        }

        //追加代码将转移目录也创建好
        val chunksDirStr = Globals.goodCacheDir.absolutePath + File.separatorChar + TEMP_CACHE_CHUNKS_DIR
        val chunksDir = File(chunksDirStr)
        //logdNoFile { "$addrTag make chunks Dir $chunksDirStr" }
        if (!chunksDir.exists()) {
            chunksDir.mkdirs()
        }

        this.tempFiles = ArrayList<TempFile>()
    }

    override fun clear() {
        //logdNoFile{"$addrTag clear it."}
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
