package com.au.module_okhttp

import com.au.module_android.Globals
import com.au.module_okhttp.beans.OkhttpBuildParams
import com.au.module_okhttp.beans.OkhttpInitParams
import com.au.module_okhttp.creator.TrustAllCertsManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

object OkhttpGlobal {
    /**
     * 获取okhttpClient对象
     * @param timeOutMode 0 使用常规15秒的。默认。
     *                    1 使用45秒的mid版。
     *                    2 使用5分钟的long版。
     */
    fun okHttpClient(timeOutMode: Int = 0) = when (timeOutMode) {
        1 -> midTimeoutOkHttpClient()
        2 -> longTimeoutOkHttpClient()
        else -> okHttpClient
    }

    private var mParams : OkhttpInitParams? = null

    private val params
        get() = mParams ?: OkhttpInitParams().also { mParams = it }

    /**
     * 如果有必要则在application尽早初始化。
     */
    fun initParams(params: OkhttpInitParams) {
        this.mParams = params
    }

    private val okHttpClient by lazy {
        createCertOkHttpBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .also {
                params.okhttpExtraBuilder?.invoke(it)
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
    private fun longTimeoutOkHttpClient() : OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    /**
     * 一次性的，超时较长的，通过newBuilder() 浅克隆使用的okhttpClient
     */
    private fun midTimeoutOkHttpClient() : OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    /** 传入 Array(1) 为1的空数组即可。
     * 返回okhttp的设置参数。
     */
    internal fun createSSLSocketFactory() : SSLSocketFactory {
        val cert = TrustAllCertsManager()

        val ssfFactory: SSLSocketFactory
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf(cert), SecureRandom())
        ssfFactory = sc.socketFactory
        return ssfFactory
    }

    /**
     * 添加证书相关设置
     */
    internal fun createCertOkHttpBuilder(params: OkhttpBuildParams? = null) : OkHttpClient.Builder {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        val okHttpEnableTrustAllCertificates = params?.okHttpEnableTrustAllCertificates ?: this.params.okHttpEnableTrustAllCertificates
        val okHttpCacheSize = params?.okHttpCacheSize ?: this.params.okHttpCacheSize
        val okHttpCookieJar = params?.okHttpCookieJar ?: this.params.okHttpCookieJar

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
}