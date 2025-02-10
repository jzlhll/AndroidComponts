package com.au.module_okhttp.creator

import com.au.module_android.utils.awaitAny
import com.au.module_android.utils.awaitOnIoThread
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.unsafeLazy
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import okio.use
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 下载文件
 */
suspend inline fun OkHttpClient.downloadFile(
    url: String,
    dirPath: String,
    fileName: String,
    byteArraySize: Int = 1024,
    deleteFileIfNoSuccess: Boolean = true,
    crossinline progressListener: (
        downloadLen: Long,
        totalLen: Long,
        progress: Float
    ) -> Unit = { _, _, _ -> },
): File? {
    return downloadFile(
        Request.Builder()
            .url(url)
            .build(),
        dirPath,
        fileName,
        byteArraySize,
        deleteFileIfNoSuccess,
        progressListener
    )
}

/**
 * 下载文件
 */
suspend inline fun OkHttpClient.downloadFile(
    request: Request,
    dirPath: String,
    fileName: String,
    byteArraySize: Int = 1024,
    deleteFileIfNoSuccess: Boolean = true,
    crossinline progressListener: (
        downloadLen: Long,
        totalLen: Long,
        progress: Float
    ) -> Unit = { _, _, _ -> },
): File? {
    val dir = dirPath.trimEnd(File.separatorChar) + File.separatorChar
    val file = File("${dir}${fileName.trimStart(File.separatorChar)}")
    val success = awaitAny<FileOutputStream> {
        val dirFile = File(dir)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        it.resume(FileOutputStream(file))
    }.use {
        downloadRequest(
            request, byteArraySize, progressListener
        ) { buf, off, len ->
            it.write(buf, off, len)
        } == null
    }
    //如果没有下载成功，删除文件
    if (deleteFileIfNoSuccess && !success) {
        file.delete()
        return null
    }
    return file
}

/**
 * 监听下载进度,支持协程取消
 */
suspend inline fun OkHttpClient.downloadRequest(
    downRequest: Request,
    byteArraySize: Int = 1024,
    crossinline progressListener: (
        downloadLen: Long,
        totalLen: Long,
        progress: Float
    ) -> Unit = { _, _, _ -> },
    crossinline block: (buf: ByteArray, off: Int, len: Int) -> Unit
): Throwable? {
    return ignoreError {
        awaitOnIoThread {
            val call = this@downloadRequest.newCall(downRequest)
            it.invokeOnCancellation { error ->
                call.cancel()
            }
            val response = call.execute()
            val body = response.body
            if (response.code != 200 || body == null) {
                it.resumeWithException(Throwable(response.message))
                return@awaitOnIoThread
            }
            val byteStream = body.byteStream()
            val total = body.contentLength()
            var len: Int
            val buf = ByteArray(byteArraySize)
            var downloadSize = 0L
            while ((byteStream.read(buf).apply { len = this } > 0)) {
                block.invoke(buf, 0, len)
                downloadSize += len

                progressListener.invoke(
                    downloadSize,
                    total,
                    downloadSize * 100f / total
                )

            }
            progressListener.invoke(
                downloadSize,
                total,
                100f
            )
            it.resume(null)
        }
    }
}

//设置了这个拦截可以直接在响应里面读取，否则会读取完网络数据才有响应
open class ProgressResponseBody(
    private val responseBody: ResponseBody,
    val progressListener: Function2<Long, Long, Unit>
) : ResponseBody() {
    private val bufferedSource: BufferedSource by unsafeLazy { source(responseBody.source()).buffer() }

    override
    fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override
    fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override
    fun source(): BufferedSource {
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                progressListener.invoke(totalBytesRead, contentLength())
                return bytesRead
            }
        }
    }
}