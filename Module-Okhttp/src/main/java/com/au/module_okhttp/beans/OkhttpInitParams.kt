package com.au.module_okhttp.beans

import com.au.module_okhttp.creator.AbsCookieJar
import com.au.module_okhttp.interceptors.PretreatmentInterceptor
import com.au.module_okhttp.interceptors.SimpleRetryInterceptor
import okhttp3.OkHttpClient

data class OkhttpInitParams(
    /**
     * 额外的okhttp builder设置。必须完整拷贝原来的2个拦截器顺序不变，填充代码部分。
     */
    var okhttpExtraBuilder : ((OkHttpClient.Builder)->Unit)? = { builder->
        builder.addInterceptor(SimpleRetryInterceptor(
            headersResetBlock = { request->
                request //填充。更改request的部分参数，比如时间戳等信息
            },
            timestampOffsetBlock = { timestampOffset->
                //填充。将timestampOffset进行存储。用于后续请求传参使用。
            },
            tokenExpiredBlock = { msg->
                //填充。仅仅是一个提醒。tokenExpire过期的时候，给出一个全局的通知。具体的那个请求还是抛异常。
            }
        ))
        builder.addInterceptor(PretreatmentInterceptor())
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