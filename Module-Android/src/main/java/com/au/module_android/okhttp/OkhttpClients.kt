package com.au.module_android.okhttp

import com.au.module_android.Globals
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
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
    private fun createCertOkHttpBuilder() : OkHttpClient.Builder {
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
            builder.cache(Cache(File(Globals.goodCacheDir, "okhttpcache"), cacheSize))
        }
        enableCookieJar?.let { builder.cookieJar(it) }
        return builder
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