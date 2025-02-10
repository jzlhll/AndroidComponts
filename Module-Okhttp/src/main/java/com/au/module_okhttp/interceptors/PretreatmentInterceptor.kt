package com.au.module_okhttp.interceptors
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
            throw AuResponseErrorException(
                responseCode,
                "$error($responseCode)"
            )
        }

        val responseBody = response.body ?: return response

        if (!bodyEncoded(response.headers)) {
            val source = responseBody.source()
            source.request(Long.Companion.MAX_VALUE) // Buffer the entire body.
            val buffer: Buffer = source.buffer

            var charset: Charset? = utf8Charset
            val contentType: MediaType? = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(utf8Charset)
                } catch (_: UnsupportedCharsetException) {
                    return response
                }
            }

            if (!isPlaintext(buffer)) {
                return response
            }

            val contentLength = responseBody.contentLength()
            if (contentLength != 0L) {
                val result: String? = buffer.clone().readString(charset ?: utf8Charset)
                if (result.isNullOrEmpty()) {
                    return response
                }
                var jsonObject = JSONObject(result)
                val code = jsonObject.optString("code")
                when (code) {
                    CODE_OK -> {
                        return response
                    }

                    CODE_TIMESTAMP_ERROR -> {
                        val msg = if(jsonObject.has("msg")) jsonObject.getString("msg") else result
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
        }

        return response
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
    fun checkHttpResponseCode(code: Int) : String? = when (code) {
        200 -> null
        202 -> "服务器已经接收请求，但尚未处理"
        400 -> "服务器不理解请求的语法"
        401 -> "请求身份验证"
        403 -> "服务器拒绝了请求"
        404 -> "服务器找不到请求网页"
        405 -> "服务器禁用了请求指定的方法"
        408 -> "服务器等候请求，发生了超时"
        500 -> "服务器遇到错误，无法去完成请求"
        501 -> "服务器无法识别请求方法"
        502 -> "服务器作为网关或代理，从上游服务器收到无效的响应"
        503 -> "服务器正在升级"
        504 -> "服务器作为网关或代理，没能及时从上游服务器收到请求"
        505 -> "服务器不支持请求所用的http版本协议"
        else -> "未知错误"
    }
}