package com.au.module_okhttp.beans

import com.au.module_okhttp.creator.AbsCookieJar
import com.au.module_okhttp.interceptors.PretreatmentInterceptor
import com.au.module_okhttp.interceptors.SimpleRetryInterceptor
import okhttp3.OkHttpClient

data class OkhttpInitParams(
    /**
     * 额外的okhttp builder设置
     */
    var okhttpExtraBuilder : ((OkHttpClient.Builder)->Unit)? = {
        it.addInterceptor(SimpleRetryInterceptor(
            headersResetBlock = {
                it
            },
            timestampOffsetBlock = {
            }
        ))
        it.addInterceptor(PretreatmentInterceptor())
    },

    /**
     * 是否信任所有证书,默认不信任，为了传输安全
     * okhttp（必须在创建之前使用）
     *
     * 比如一般的request，默认false。
     * 比如图片，直链下载链接，传入true。
     */
    var okHttpEnableTrustAllCertificates: Boolean = false,

    /**
     * 自定义cookie管理
     * okhttp（必须在创建之前使用）
     */
    var okHttpCookieJar: AbsCookieJar? = null,

    /**
     * 设置okhttp缓存，如果大于0，开启缓存
     */
    var okHttpCacheSize: Long = 0L,
    ////////////////////////////////////////////////////
)