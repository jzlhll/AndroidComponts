package com.au.module_okhttp.creator

import com.au.module_android.utils.awaitOnIoThread
import com.au.module_android.utils.parseJson
import com.au.module_okhttp.exceptions.AuNoNetworkException
import com.au.module_okhttp.exceptions.AuResponseErrorException
import com.au.module_okhttp.OkhttpGlobal.okHttpClient
import com.au.module_okhttp.beans.ParamsStrRequestBody
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSink
import java.net.UnknownHostException
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.Charsets.UTF_8

/**
 * 发送请求，并等待Response
 */
suspend inline fun Request.awaitHttpResponse(
    client: OkHttpClient = okHttpClient(),
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
                AuNoNetworkException()
            } else e
            it.resumeWithException(ne)
        }
    }
}

/**
 * 请将Request直接写完再传入，避免写的过于复杂。
 * 发送请求，并等待转换结果
 */
suspend inline fun <reified T> Request.awaitHttpResult(
    client: OkHttpClient = okHttpClient(),
    crossinline callBlock: ((Call) -> Unit) = { },
): T {
    return awaitHttpResponse(client, callBlock).parseJson()
}

/**
 * 请将Request直接写完再传入，避免写的过于复杂。
 * 发送请求，并等待转换结果
 */
suspend fun Request.awaitHttpResultStr(
    client: OkHttpClient = okHttpClient(),
    callBlock: ((Call) -> Unit) = { },
): String? {
    return awaitHttpResponse(client, callBlock).parseJsonStr()
}

/**
 * 结果转换
 */
inline fun <reified T> Response.parseJson() : T {
    return when (T::class.java) {
        Response::class.java -> this as T
        ResponseBody::class.java -> this.body as T
        String::class.java -> this.body!!.string() as T
        else -> {
            val bodyStr = this.body?.string()
            if (bodyStr.isNullOrBlank()) {
                throw AuResponseErrorException(this.code, this.message)
            } else {
                bodyStr.parseJson<T>()
            }
        }
    }
}

/**
 * 结果转换
 */
fun Response.parseJsonStr() : String? {
    return this.body?.string()
}

/**
 * 创建RequestBody。重构requestBody。
 *  fun String.toRequestBody(contentType: MediaType? = null)
 *  参考而来。
 */
fun String.toParamsStrRequestBody(): ParamsStrRequestBody {
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
    return object : ParamsStrRequestBody(this@toParamsStrRequestBody) {
        override fun contentType() = finalContentType

        override fun contentLength() = bytes.size.toLong()

        override fun writeTo(sink: BufferedSink) {
            sink.write(bytes, 0, bytes.size)
        }
    }
}