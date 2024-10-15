package com.au.module_android.okhttp

import android.util.Log
import com.au.module_android.BuildConfig
import com.au.module_android.Globals
import okhttp3.Cache
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.readBomAsCharset
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

class OkhttpClients {
    companion object {
        /** 传入 Array(1) 为1的空数组即可。
         * 返回okhttp的设置参数。
         */
        fun createSSLSocketFactory(): SSLSocketFactory {
            val cert = TrustAllCertsManager()

            val ssfFactory: SSLSocketFactory
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf(cert), SecureRandom())
            ssfFactory = sc.socketFactory
            return ssfFactory
        }
    }

    /**
     * 添加证书相关设置
     */
    private fun createCertOkHttpBuilder(): OkHttpClient.Builder {
        return createCertOkHttpBuilder(
            Globals.okHttpEnableTrustAllCertificates,
            Globals.okHttpCookieJar,
            Globals.okHttpCacheSize
        )
    }

    private fun createCertOkHttpBuilder(enableCert: Boolean, enableCookieJar: AbsCookieJar?, cacheSize: Long): OkHttpClient.Builder {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (enableCert) {
            //下面两行信任所有证书
            builder.sslSocketFactory(
                createSSLSocketFactory(),
                TrustAllCertsManager()
            )
            builder.hostnameVerifier(TrustAllCertsManager.TrustAllHostnameVerifier())
        }
        if (cacheSize > 0) {
            //如果缓存的长度大于0，设置缓存
            builder.cache(Cache(File(Globals.app.externalCacheDir ?: Globals.app.cacheDir, "okhttpcache"), cacheSize))
        }
        Globals.okHttpCookieJar?.let { builder.cookieJar(it) }
        return builder
    }

    /**
     * 打印Http请求日志
     */
    fun httpRequestLog(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val bodyStr = StringBuilder()
        val requestParams = when (val body = request.body) {
            is FormBody -> {
                repeat(body.size) {
                    bodyStr.append(body.encodedName(it))
                    bodyStr.append(":")
                    bodyStr.append(body.encodedValue(it))
                    bodyStr.append(",")
                }
                bodyStr.toString()
            }

            else -> {
                bodyStr.append(body.toString())
                bodyStr.append("\n不是FormBody，不支持打印请求参数").toString()
            }
        }
        val requestLog =
            "发送请求：${request.url}\n请求参数：${requestParams}\n请求头:\n${request.headers}"
        if(BuildConfig.DEBUG) Log.d("OkhttpApi", requestLog)
        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        val endTime = System.nanoTime()
        val oldSource = response.body?.source() ?: return response
        oldSource.request(java.lang.Long.MAX_VALUE)
        val buffer = oldSource.buffer.clone()
        val charset =
            buffer.readBomAsCharset(
                response.body?.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            )
        val responseStr = if (
            response.header("Content-Encoding")?.contains("gzip", true) == true ||
            response.header("Accept-Encoding")?.contains("gzip", true) == true
        ) {
            GZIPInputStream(buffer.inputStream()).bufferedReader(charset).readText()
        } else {
            buffer.inputStream().bufferedReader(charset).readText()
        }
        val responseLog = "{\'响应链接\':\'${response.request.url}\'," +
                "\'响应请求头\':\'${response.headers}\'," +
                "\'响应时间\':\'${(endTime - startTime) / 1e6} ms\'," +
                "\'响应数据\':${responseStr}}"
        if(BuildConfig.DEBUG) Log.d("OkhttpApi", responseLog)
        return response
    }

    /**
     * 快速响应的okhttp单例。5秒超时。
     */
    val quickOkHttpClient by lazy {
            createCertOkHttpBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .also {
                Globals.okhttpExtraBuilder?.invoke(it)
                //最后添加日志打印，保证打印最全
    //                    doOnlyDebug {
    //                        it.addNetworkInterceptor(::httpRequestLog)
    //                    }
            }
            .build()
    }

    /**
     * okHttpClient，通过[BaseLibraryConfig.onCreateOkhttpClient]可自定义
     * 全局单例
     */
    val okHttpClient by lazy {
             createCertOkHttpBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .also {
                Globals.okhttpExtraBuilder?.invoke(it)
                //最后添加日志打印，保证打印最全
    //                    doOnlyDebug {
    //                        it.addNetworkInterceptor(::httpRequestLog)
    //                    }
            }
            .build()
    }

    /**
     * 一次性的，超时长的，通过newBuilder() 浅克隆使用的okhttpClient
     */
    fun longTimeoutOkHttpClient() : OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    /**
     * 一次性的，超时较长的，通过newBuilder() 浅克隆使用的okhttpClient
     */
    fun midTimeoutOkHttpClient() : OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }
}