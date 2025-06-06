package com.allan.mydroid.nanohttp

import com.allan.mydroid.globals.nanoTempCacheChunksDir
import com.allan.mydroid.globals.nanoTempCacheDir
import com.allan.mydroid.globals.nanoTempCacheMergedDir
import fi.iki.elonen.NanoHTTPD.DefaultTempFile
import fi.iki.elonen.NanoHTTPD.TempFile
import fi.iki.elonen.NanoHTTPD.TempFileManager
import fi.iki.elonen.NanoHTTPD.TempFileManagerFactory
import java.io.File

class MyDroidTempFileMgrFactory : TempFileManagerFactory {
    override fun create() = MyDroidTempFileManager()
}

class MyDroidTempFileManager : TempFileManager {
    private val tmpdir: File = File(nanoTempCacheDir())

    private val tempFiles: MutableList<TempFile>

    init {
        //logdNoFile { "$addrTag make tmp dir $tmpDirStr" }
        if (!tmpdir.exists()) {
            tmpdir.mkdirs()
        }

        //追加代码将转移目录也创建好
        val chunksDir = File(nanoTempCacheChunksDir())
        //logdNoFile { "$addrTag make chunks Dir $chunksDirStr" }
        if (!chunksDir.exists()) {
            chunksDir.mkdirs()
        }

        val fileDir = File(nanoTempCacheMergedDir())
        if (!fileDir.exists()) {
            fileDir.mkdirs()
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
