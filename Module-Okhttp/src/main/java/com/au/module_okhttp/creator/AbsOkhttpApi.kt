package com.au.module_okhttp.creator

import com.au.module_android.api.ResultBean
import com.au.module_android.json.fromJson
import com.au.module_android.json.toJsonString
import com.au.module_okhttp.OkhttpGlobal
import com.au.module_okhttp.exceptions.AuNoBaseUrlException
import okhttp3.Request
import kotlin.jvm.Throws

abstract class AbsOkhttpApi {
    abstract fun getBaseUrl():String
    open fun setHeadMap(request: Request.Builder) {
    }

//    open fun generateRequestHeaderMap(paramsStr:String, xh5ctrl: String?) : SortedMap<String, String> {
//        val token = getToken() ?: "none"
//        val appKey = getAppKey()
//        val appId = getAppId()
//        val lang = getLang()
//        val region = getRegion()
//        val headMap = sortedMapOf(
//            "x-appId" to appId,
//            "x-appVersion" to BaseGlobalConst.versionCode.toString(),
//            "x-nonce" to UUID.randomUUID().toString().replace("-", "").lowercase(),
//            "x-timestamp" to (System.currentTimeMillis() + timestampOffset).toString(),
//            "x-token" to token,
//            "x-deviceSn" to androidUtdid
//        )
//        if (lang.isNotEmpty()) headMap["x-lang"] = lang
//        if (region.isNotEmpty()) headMap["x-region"] = region
//        //sign
//        val signSb = StringBuilder()
//        signSb.append(appKey)
//        signSb.append("|")
//        for ((key, value) in headMap) {
//            signSb.append(key)
//            signSb.append("=")
//            signSb.append(value)
//            signSb.append(";")
//        }
//        //sign:删除最后一个分号
//        signSb.deleteCharAt(signSb.lastIndex)
//        if (paramsStr.isNotBlank()) {
//            signSb.append("|")
//            signSb.append(paramsStr)
//        }
//        val sign = signSb.toString().replace("\n", "").md5()
//        headMap["x-sign"] = sign
//
//        if (!xh5ctrl.isNullOrEmpty()) headMap["x-h5ctrl"] = xh5ctrl
//        return headMap
//    }
//
    @Throws
    open suspend fun requestApi(
        apiUri:String,
        params: Any? = null,
        timeOutMode:Int = 0,
    ): String {
        val baseUrl = getBaseUrl()
        if (baseUrl.isEmpty()) {
            throw AuNoBaseUrlException("No Base Url!")
        }

        //修改此处代码，实现了可以直接传入params为String。之前必须是对象
        val paramsStr = params as? String ?: (params?.toJsonString() ?: "{}")

        val url = "${baseUrl.trimEnd('/')}/${apiUri.trimStart('/')}"

        val builder = Request.Builder()
            .url(url)
            .post(paramsStr.toParamsStrRequestBody())
            .also {
                setHeadMap(it)
            }
        val resultStr = builder.build().awaitHttpResultStr(OkhttpGlobal.okHttpClient(timeOutMode)) ?: "{}"
        return resultStr
    }

    suspend inline fun <reified T> requestResult(
        apiUri:String,
        params: Any? = null,
        timeOutMode:Int = 0,
    ): ResultBean<T>? {
        val resultStr = requestApi(apiUri, params, timeOutMode)
        return resultStr.fromJson<ResultBean<T>>()
    }

    suspend inline fun <reified T> requestResultData(
        apiUri:String,
        params: Any? = null,
        timeOutMode:Int = 0,
    ): T? {
        val resultStr = requestApi(apiUri, params, timeOutMode)
        return resultStr.fromJson<ResultBean<T>>()?.data
    }
}