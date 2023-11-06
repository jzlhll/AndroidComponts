package com.au.module_android.glide

import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TrustAllCerts : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

    class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    /** 传入 Array(1) 为1的空数组即可。
     * 返回okhttp的设置参数。
     */
    fun createSSLSocketFactory(): SSLSocketFactory {
        val cert = TrustAllCerts()

        val ssfFactory: SSLSocketFactory
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf(cert), SecureRandom())
        ssfFactory = sc.socketFactory
        return ssfFactory
    }
}
