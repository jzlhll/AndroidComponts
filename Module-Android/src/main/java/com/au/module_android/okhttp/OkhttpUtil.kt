package com.au.module_android.okhttp

import com.au.module_android.Globals
import com.au.module_android.json.fromJson
import com.au.module_android.utils.awaitOnIoThread
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSink
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.Charsets.UTF_8

/**
 * 创建RequestBody。重构requestBody。
 *  fun String.toRequestBody(contentType: MediaType? = null)
 *  参考而来。
 */
fun String.toRequestJsonBody(): ParamsStrRequestBody {
    var charset: Charset = UTF_8
    val contentType: MediaType? = "application/json".toMediaTypeOrNull()
    var finalContentType: MediaType? = contentType
    if (contentType != null) {
        val resolvedCharset = contentType.charset()
        if (resolvedCharset == null) {
            charset = UTF_8
            finalContentType = "$contentType; charset=utf-8".toMediaTypeOrNull()
        } else {
            charset = resolvedCharset
        }
    }
    val bytes = toByteArray(charset)
    return object : ParamsStrRequestBody(this@toRequestJsonBody) {
        override fun contentType() = finalContentType

        override fun contentLength() = bytes.size.toLong()

        override fun writeTo(sink: BufferedSink) {
            sink.write(bytes, 0, bytes.size)
        }
    }
}

/**
 * 发送请求，并等待Response
 */
suspend inline fun Request.awaitHttpResponse(
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { }
): Response {
    return awaitOnIoThread {
        val call = client.newCall(this)
        callBlock.invoke(call)
        it.invokeOnCancellation { error ->
            call.cancel()
        }
        try {
            it.resume(call.execute())
        } catch (e: Throwable) {
            val ne = if (e is UnknownHostException) {
                NoNetworkException()
            } else e
            it.resumeWithException(ne)
        }
    }
}

/**
 * 发送请求，并等待转换结果
 */
suspend inline fun <reified T> Request.awaitHttpResult(
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
    transformer: Function1<Response, T?> = { null }
): T? {
    return awaitHttpResponse(client, callBlock).fromJson(transformer)
}

/**
 * 发起http请求
 */
suspend inline fun <reified T> String.awaitHttp(
    requestBody: RequestBody? = null/*不为null，就代表post请求*/,
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
    requestBuildBlock: ((Request.Builder) -> Request.Builder) = { it },
    transformer: Function1<Response, T?> = { null }
): T? {
    return awaitHttp(
        Request.Builder().url(this),
        requestBody,
        client,
        callBlock,
        requestBuildBlock,
        transformer
    )
}

/**
 * 发起http请求
 */
suspend inline fun <reified T> URL.awaitHttp(
    requestBody: RequestBody? = null/*不为null，就代表post请求*/,
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
    requestBuildBlock: ((Request.Builder) -> Request.Builder) = { it },
    transformer: Function1<Response, T?> = { null }
): T? {
    return awaitHttp(
        Request.Builder().url(this),
        requestBody,
        client,
        callBlock,
        requestBuildBlock,
        transformer
    )
}

/**
 * 发起http请求
 */
suspend inline fun <reified T> HttpUrl.awaitHttp(
    requestBody: RequestBody? = null/*不为null，就代表post请求*/,
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
    requestBuildBlock: ((Request.Builder) -> Request.Builder) = { it },
    transformer: Function1<Response, T?> = { null }
): T? {
    return awaitHttp(
        Request.Builder().url(this),
        requestBody,
        client,
        callBlock,
        requestBuildBlock,
        transformer
    )
}

/**
 * 发起http请求
 */
suspend inline fun <reified T> awaitHttp(
    build: Request.Builder,
    requestBody: RequestBody? = null/*不为null，就代表post请求*/,
    client: OkHttpClient = Globals.okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
    requestBuildBlock: ((Request.Builder) -> Request.Builder) = { it },
    transformer: Function1<Response, T?> = { null }
): T? {
    return build
        .also {
            if (requestBody != null) {
                it.post(requestBody)
            } else {
                it.get()
            }
            requestBuildBlock.invoke(it)
        }.build()
        .awaitHttpResult(client, callBlock, transformer)
}

/**
 *检查响应码，没错错误，返回null
 */
fun Int.checkHttpResponseCode(): OKHttpResponseException? {
    return when (this) {
        200 -> null
        202 -> OKHttpResponseException(this, "服务器已经接收请求，但尚未处理")
        400 -> OKHttpResponseException(this, "服务器不理解请求的语法")
        401 -> OKHttpResponseException(this, "请求身份验证")
        403 -> OKHttpResponseException(this, "服务器拒绝了请求")
        404 -> OKHttpResponseException(this, "服务器找不到请求网页")
        405 -> OKHttpResponseException(this, "服务器禁用了请求指定的方法")
        408 -> OKHttpResponseException(this, "服务器等候请求，发生了超时")
        500 -> OKHttpResponseException(this, "服务器遇到错误，无法去完成请求")
        501 -> OKHttpResponseException(this, "服务器无法识别请求方法")
        502 -> OKHttpResponseException(this, "服务器作为网关或代理，从上游服务器收到无效的响应")
        503 -> OKHttpResponseException(this, "服务器正在升级")
        504 -> OKHttpResponseException(this, "服务器作为网关或代理，没能及时从上游服务器收到请求")
        505 -> OKHttpResponseException(this, "服务器不支持请求所用的http版本协议")
        else -> OKHttpResponseException(this, "未知错误")
    }
}

/**
 * 结果转换
 */
inline fun <reified T> Response.fromJson(
    transformer: Function1<Response, T?> = { null }
): T? {
    val result = transformer.invoke(this)
    if (result != null) {
        return result
    }

    //检查错误码
    val oKHttpResponseException = this.code.checkHttpResponseCode()
    if (oKHttpResponseException != null) {
        throw OKHttpResponseException(
            oKHttpResponseException.errorCode,
            "${oKHttpResponseException.message}(${oKHttpResponseException.errorCode})"
        )
    }

    return when (T::class.java) {
        Response::class.java -> this as T
        ResponseBody::class.java -> this.body as T
        String::class.java -> this.body!!.string() as T
        else -> {
            val bodyStr = this.body?.string()
            if (bodyStr.isNullOrBlank()) {
                throw OKHttpResponseException(this.code, this.message)
            } else {
                bodyStr.fromJson<T>()
            }
        }
    }
}

/**
 * 下载文件
 */
suspend fun OkHttpClient.downloadFile(
    url: String,
    dirPath: String,
    fileName: String,
    byteArraySize: Int = 1024,
    deleteFileIfNoSuccess: Boolean = true,
    progressListener: (
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
suspend fun OkHttpClient.downloadFile(
    request: Request,
    dirPath: String,
    fileName: String,
    byteArraySize: Int = 1024,
    deleteFileIfNoSuccess: Boolean = true,
    progressListener: (
        downloadLen: Long,
        totalLen: Long,
        progress: Float
    ) -> Unit = { _, _, _ -> },
): File? {
    val dir = dirPath.trimEnd(File.separatorChar) + File.separatorChar
    val file = File("${dir}${fileName.trimStart(File.separatorChar)}")
    val success = suspendCancellableCoroutine {
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
suspend fun OkHttpClient.downloadRequest(
    downRequest: Request,
    byteArraySize: Int = 1024,
    progressListener: (
        downloadLen: Long,
        totalLen: Long,
        progress: Float
    ) -> Unit = { _, _, _ -> },
    block: (buf: ByteArray, off: Int, len: Int) -> Unit
): Throwable? {
    try {
        return awaitOnIoThread {
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
    } catch (e:Exception) {
        e.printStackTrace()
    }
    return null
}