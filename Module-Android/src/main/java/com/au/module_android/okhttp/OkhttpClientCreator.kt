package com.au.module_android.okhttp

import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

class OkhttpClientCreator {
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

    ////////////////////////参数
    ///////
    ////////////////////////////////////////////
    /**
     * 额外的okhttp builder设置
     */
    var okhttpExtraBuilder : ((OkHttpClient.Builder)->Unit)? = null

    /**
     * 是否信任所有证书,默认不信任，为了传输安全
     * okhttp（必须在创建之前使用）
     */
    var okHttpEnableTrustAllCertificates = false

    /**
     * 自定义cookie管理
     * okhttp（必须在创建之前使用）
     */
    var okHttpCookieJar: AbsCookieJar? = null

    /**
     * 设置okhttp缓存，如果大于0，开启缓存
     */
    var okHttpCacheSize = 0L
    ////////////////////////////////////////////////////

    /**
     * 添加证书相关设置
     */
    private fun createCertOkHttpBuilder() : OkHttpClient.Builder {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (okHttpEnableTrustAllCertificates) {
            //下面两行信任所有证书
            builder.sslSocketFactory(
                createSSLSocketFactory(),
                TrustAllCertsManager()
            )
            builder.hostnameVerifier(TrustAllCertsManager.TrustAllHostnameVerifier())
        }
        if (okHttpCacheSize > 0) {
            //如果缓存的长度大于0，设置缓存
            builder.cache(Cache(File(Globals.goodCacheDir, "okhttpcache"), okHttpCacheSize))
        }
        okHttpCookieJar?.let { builder.cookieJar(it) }
        return builder
    }

    val okHttpClient by lazy {
             createCertOkHttpBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .also {
                okhttpExtraBuilder?.invoke(it)
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