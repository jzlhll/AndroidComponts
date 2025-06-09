package com.allan.mydroid.api

import com.au.module_cached.delegate.AppDataStoreLongCache
import com.au.module_okhttp.creator.AbsOkhttpApi

object Api : AbsOkhttpApi() {
    var timestampOffset by AppDataStoreLongCache("api_timestampOffset", 0L)

    /**
     * 当前的baseUrl
     */
    var currentBaseUrl : String = ""

    override fun getBaseUrl(): String {
        return currentBaseUrl
    }
}