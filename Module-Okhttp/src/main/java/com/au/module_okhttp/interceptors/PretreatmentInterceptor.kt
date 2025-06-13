package com.au.module_okhttp.interceptors
import com.au.module_android.Globals
import com.au.module_okhttp.exceptions.AuApiException
import com.au.module_okhttp.exceptions.AuResponseErrorException
import com.au.module_okhttp.exceptions.AuTokenExpiredException
import com.au.module_okhttp.exceptions.AuTimestampErrorException
import com.au.module_okhttp.beans.CODE_OK
import com.au.module_okhttp.beans.CODE_TIMESTAMP_ERROR
import com.au.module_okhttp.beans.CODE_TOKEN_EXPIRED
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONObject
import java.io.EOFException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

/**
 * 拦截器，对响应进行预处理。必须放在OkhttpSimpleRetryInterceptor之后。
 * 1. 检查错误码
 * 2. 检查token是否过期
 * 3. 时间戳偏移纠正
 */
class PretreatmentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val responseCode = response.code
        //1. 检查错误码
        val error = checkHttpResponseCode(responseCode)
        if (error != null) {
            throw AuResponseErrorException(responseCode, "$error, ($responseCode)")
        }

        val responseBody = response.body ?: return response

        if (!bodyEncoded(response.headers)) {
            checkWithBody(responseBody)
        }

        return response
    }

    private fun checkWithBody(responseBody: ResponseBody) {
        val source = responseBody.source()
        source.request(Long.Companion.MAX_VALUE) // Buffer the entire body.
        val buffer: Buffer = source.buffer
        if (!isPlaintext(buffer)) {
            return
        }

        var charset: Charset? = utf8Charset
        val contentType: MediaType? = responseBody.contentType()
        if (contentType != null) {
            try {
                charset = contentType.charset(utf8Charset)
            } catch (_: UnsupportedCharsetException) {
                return
            }
        }

        val contentLength = responseBody.contentLength()
        if (contentLength != 0L) {
            checkContentLength(buffer, charset)
            return
        }
    }

    private fun checkContentLength(buffer: Buffer, charset: Charset?) {
        val result: String? = buffer.clone().readString(charset ?: utf8Charset)
        if (result.isNullOrEmpty()) {
            return
        }
        var jsonObject = JSONObject(result)
        val code = jsonObject.optString("code")
        when (code) {
            CODE_OK -> {
                return
            }

            CODE_TIMESTAMP_ERROR -> {
                val msg = if (jsonObject.has("msg")) jsonObject.getString("msg") else result
                if (jsonObject.has("data")) {//处理时间戳的偏移
                    val data = jsonObject.optJSONObject("data")
                    if (data != null && data.has("timestamp")) {
                        val timestamp = data.getLong("timestamp")
                        val timestampOffset = timestamp - System.currentTimeMillis()
                        throw AuTimestampErrorException(timestampOffset, true, msg)
                    } else {
                        throw AuTimestampErrorException(0, false, msg)
                    }
                } else {
                    throw AuTimestampErrorException(0, false, msg)
                }
            }

            CODE_TOKEN_EXPIRED -> {
                if (jsonObject.has("msg")) {
                    val msg = jsonObject.getString("msg")
                    throw AuTokenExpiredException(msg)
                } else {
                    throw AuTokenExpiredException(result)
                }
            }

            else -> {
                var da = if (jsonObject.has("data")) jsonObject.getString("data") else null
                if (da == "null" || da.isNullOrEmpty()) {
                    da = null
                }
                if (jsonObject.has("msg")) {
                    val msg = jsonObject.getString("msg")
                    throw AuApiException(code, msg, da)
                } else {
                    throw AuApiException(code, result, da)
                }
            }
        }
        //Log.d("okhttp", " response.url $response.request.url response.body():$result")
        //得到所需的string，还可以进一步处理
        //***********************do something*****************************
    }

    private val utf8Charset: Charset = Charset.forName("UTF-8")

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding: String? = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    @Throws(EOFException::class)
    fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount: Long = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint: Int = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    /**
     *检查响应码，没错错误，返回null
     */
    fun checkHttpResponseCode(code: Int): String? = when (code) {
        200 -> null
        202 -> Globals.getString(com.au.module_okhttp.R.string.http_202)
        400 -> Globals.getString(com.au.module_okhttp.R.string.http_400)
        401 -> Globals.getString(com.au.module_okhttp.R.string.http_401)
        403 -> Globals.getString(com.au.module_okhttp.R.string.http_403)
        404 -> Globals.getString(com.au.module_okhttp.R.string.http_404)
        405 -> Globals.getString(com.au.module_okhttp.R.string.http_405)
        408 -> Globals.getString(com.au.module_okhttp.R.string.http_408)
        500 -> Globals.getString(com.au.module_okhttp.R.string.http_500)
        501 -> Globals.getString(com.au.module_okhttp.R.string.http_501)
        502 -> Globals.getString(com.au.module_okhttp.R.string.http_502)
        503 -> Globals.getString(com.au.module_okhttp.R.string.http_503)
        504 -> Globals.getString(com.au.module_okhttp.R.string.http_504)
        505 -> Globals.getString(com.au.module_okhttp.R.string.http_505)
        else -> Globals.getString(com.au.module_okhttp.R.string.http_unknown)
    }
}