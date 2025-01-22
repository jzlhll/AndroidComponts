package com.au.module_android.okhttp

import android.os.Build
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.okhttp.beans.BaseBean
import com.au.module_android.utils.AbsRMWLCacheProperty
import com.au.module_android.utils.unsafeLazy
import com.google.gson.internal.LinkedTreeMap
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.SortedMap
import java.util.UUID

/**
 * 抽象类
 * 实现了基础的okhttp访问的常用接口。
 */
abstract class AbsOkhttpApi {
    /**
     * 提供时间戳缓存器
     */
    abstract val timestampOffsetProperty:AbsRMWLCacheProperty<Long>
    /**
     * 时间戳偏移量，用于解决服务器时间与客户端时间不一致的问题
     */
    private val timestampOffset by timestampOffsetProperty

    private val mDeviceInfo by unsafeLazy { "${Build.BRAND} ${Build.MODEL} ${Build.VERSION.RELEASE}" }
    /**
     * 设备信息
     */
    open val deviceInfo
        get() = mDeviceInfo

    open fun okhttpClient(timeOutMode:Int = 0) : OkHttpClient {
        return Globals.okHttpClient(timeOutMode)
    }

    /**
     * ApiUrl
     */
    abstract fun getUrl(): String

    /**
     * 获取接口访问的token
     */
    abstract fun getToken(): String?

    /**
     * appKey
     */
    abstract fun getAppKey(): String

    /**
     * appId
     */
    abstract fun getAppId(): String

    /**
     * 语言
     */
    abstract fun getLang(): String


    /**
     * 没有data的数据结构
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    suspend fun String.post(params: Any? = null, enableCache: Boolean = false, enableParamsCondition:Boolean = true): TyiotApiBean {
        return apiRequest(params, null, enableCache, enableParamsCondition)
    }

    /**
     * data是json对象
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    suspend inline fun <reified T> String.postResult(
        params: Any? = null,
        enableCache: Boolean = false,
        enableParamsCondition:Boolean = true
    ): T {
        return apiRequest<TyiotApiBean.Result<T>>(params, null, enableCache, enableParamsCondition).data
    }

    /**
     * data是json数组
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    suspend inline fun <reified T> String.postResultList(
        params: Any? = null,
        enableCache: Boolean = false,
        enableParamsCondition:Boolean = true
    ): List<T> {
        return apiRequest<TyiotApiBean.ResultList<T>>(
            params,
            createGsonParameterizedType {
                arrayOf(T::class.java)
            },
            enableCache,
            enableParamsCondition
        ).data
    }

    /**
     * data是分页数据
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    suspend inline fun <reified T> String.postPage(
        params: Any? = null,
        enableCache: Boolean = false,
        enableParamsCondition:Boolean = true
    ): TyiotApiBean.PageBean<T> {
        return apiRequest<TyiotApiBean.Page<T>>(
            params,
            createGsonParameterizedType {
                arrayOf(T::class.java)
            },
            enableCache,
            enableParamsCondition
        ).data
    }

    /**
     *上传文件
     */
    suspend fun String.postFile(file: TyiotApiUpFileBean): String {
        //获取亚马逊文件上传地址
        val picResult = this.postResult<LinkedTreeMap<String, String>>(
            hashMapOf(
                "contentLength" to file.file.length(),
                "fileName" to file.fileName,
            )
        )
        val response = Request.Builder()
            .addHeader("Content-Type", file.contentType)
            .url(picResult["uploadUrl"].toString())
            .put(file.file.asRequestBody(file.mediaType.toMediaType()))
            .build().awaitHttpResponse(BaseGlobalConst.longTimeoutOkHttpClient())
        if (response.code != 200) {
            throw TyiotApiError(response.code.toString(), response.message)
        }
        //返回临时地址
        return picResult["fileUrl"].toString()
    }

    /**
     *上传系统选择的Content Uri文件
     */
    suspend fun String.postUri(uriBean: TyiotApiUpUriBean): String {
        //获取亚马逊文件上传地址
        val picResult = this.postResult<LinkedTreeMap<String, String>>(
            hashMapOf(
                "contentLength" to uriBean.fileLength,
                "fileName" to uriBean.fileName,
            )
        )
        val requestBody = uriBean.uri.asInputStreamRequestBody(BaseGlobalConst.app, uriBean.fileLength, uriBean.mediaType.toMediaType())
        val response = Request.Builder()
            .addHeader("Content-Type", uriBean.contentType)
            .url(picResult["uploadUrl"].toString())
            .put(requestBody)
            .build().awaitHttpResponse(BaseGlobalConst.longTimeoutOkHttpClient())
        if (response.code != 200) {
            throw TyiotApiError(response.code.toString(), response.message)
        }
        //返回临时地址
        return picResult["fileUrl"].toString()
    }

    /**
     * 构建请求
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    suspend inline fun <reified T : TyiotApiBean> String.apiRequest(
        params: Any? = null,
        customType: GsonParameterizedType<T>? = null,
        enableCache: Boolean = false,
        enableParamsCondition: Boolean = true,
    ): T {
        //开始网络请求
        val resultStr = okhttpRequest(params, enableCache, enableParamsCondition)
        //数据格式转换
        return resultStr.formJsonString(customType)
    }

    /**
     * 获取完整的url链接
     */
    open fun getApiUrl(api:String) = "${getUrl().trimEnd('/')}/${api.trimStart('/')}"

    /**
     * 重置request其中的部分header
     */
    fun refreshRequest(request: Request) : Request {
        val xh5ctrl = request.header("x-h5ctrl")
        val url = request.url
        val paramsStr = request.body.asOrNull<ParamsStrRequestBody>()?.paramsStr ?: "{}"
        return Request.Builder()
            .url(url)
            .post(paramsStr.toRequestJsonBody())
            .also {
                val headMap = generateRequestHeaderMap(paramsStr, xh5ctrl)
                for ((key, value) in headMap) {
                    it.header(key, value)
                }
            }.build()
    }

    open fun generateRequestHeaderMap(paramsStr:String) : SortedMap<String, String> {
        val token = getToken() ?: "none"
        val appKey = getAppKey()
        val appId = getAppId()
        val lang = getLang()
        val headMap = sortedMapOf(
            "x-appId" to appId,
            //"x-appVersion" to Globals.versionCode.toString(),
            "x-nonce" to UUID.randomUUID().toString().replace("-", "").lowercase(),
            "x-timestamp" to (System.currentTimeMillis() + timestampOffset).toString(),
            "x-token" to token,
            "x-deviceSn" to androidUtdid
        )
        if (lang.isNotEmpty()) headMap["x-lang"] = lang
        if (region.isNotEmpty()) headMap["x-region"] = region
        //sign
        val signSb = StringBuilder()
        signSb.append(appKey)
        signSb.append("|")
        for ((key, value) in headMap) {
            signSb.append(key)
            signSb.append("=")
            signSb.append(value)
            signSb.append(";")
        }
        //sign:删除最后一个分号
        signSb.deleteCharAt(signSb.lastIndex)
        if (paramsStr.isNotBlank()) {
            signSb.append("|")
            signSb.append(paramsStr)
        }
        val sign = signSb.toString().replace("\n", "").md5()
        headMap["x-sign"] = sign

        if (!xh5ctrl.isNullOrEmpty()) headMap["x-h5ctrl"] = xh5ctrl
        return headMap
    }

    /**
     * 网络请求
     * @param params 请求参数
     * @param ifCache 是否保存cache
     * @param ifCacheConsiderParam 如果ifCache则会生效；默认为true，表示请求参数是否参与缓存的判断
     */
    open suspend fun okhttpRequest(
        apiName:String,
        params: Map<String, Any>? = null,
        ifCache: Boolean = false,
        ifCacheConsiderParam: Boolean = true,
        timeOutMode:Int = 0,
    ): String {
        val paramsStr = params?.toJsonString() ?: "{}"

        val url = getApiUrl(apiName)
        val builder = Request.Builder()
            .url(url)
            .post(paramsStr.toRequestJsonBody())
            .also {
                val headMap = generateRequestHeaderMap(paramsStr)
                for ((key, value) in headMap) {
                    it.header(key, value)
                }
            }

        val resultStr = withIoThread { builder.build().awaitHttpResult<String>(Globals.okHttpClient(timeOutMode)) }
        if (BuildConfig.DEBUG && paramsStr.length < 50 * 1024 && hasDebugLog(this)) {
            lodDApi("TyiotOkHttp 请求 (${this}) $url：$paramsStr", false)
        }
        if (BuildConfig.DEBUG && hasDebugLog(this)) {
            if (resultStr.length < 50 * 1024) {
                val formatJsonBeautiful = resultStr.formatJsonBeautiful()
                lodDApi(formatJsonBeautiful, true)
                largeLogWithLine("tyiot_app", formatJsonBeautiful)
            }
        }

        var jsonObject = JSONObject(resultStr)
        val finalResultStr = if (jsonObject.has("code")) {
            var code = jsonObject.getString("code")
            //检查返回码
            val fixResultStr = onHandleApiCodeTimestampErr(this, params, code, jsonObject, resultStr)
            if (fixResultStr.second) { //重新修正后的jsonObject，因为二次请求
                jsonObject = JSONObject(fixResultStr.first)
                code = if (jsonObject.has("code")) jsonObject.getString("code") else ""
            }
            onHandleApiCode(this, params, code, jsonObject, fixResultStr.first)
            fixResultStr.first
        } else {
            resultStr
        }
        //如果允许缓存，则缓存数据
        val lang = getLang()
        val region = getRegion()
        onCacheOkHttp(
            enableCache,
            enableParamsCondition,
            this,
            paramsStr,
            jsonObject,
            region,
            lang
        )
        return finalResultStr
    }

    /**
     * 缓存数据
     */
    open suspend fun onCacheOkHttp(
        enableCache: Boolean,
        enableCacheParamsCondition:Boolean,
        path: String,
        params: String,
        jsonObject: JSONObject,
        region: String,
        lang: String
    ) {
        if (!enableCache) {
            return
        }

        if (!jsonObject.has("data")) {
            return
        }

        //如果返回结果，并且支持缓存，则缓存数据
        val cacheContent = jsonObject.getString("data")
        if (cacheContent.isEmpty()) {
            return
        }

        withIoThread {
            ignoreError {
                KotleOkHttpCacheTable().apply {
                    this.url = path.getWholeUrl()
                    this.params = params
                    this.content = cacheContent
                    this.heads = ""
                    this.region = region
                    this.lang = lang
                    this.method = "post"
                }.cacheOrUpdateTyiotOkHttp(enableCacheParamsCondition)
                "成功缓存（${path}）："
            }
        }
    }

    open fun KotleOkHttpCacheTable.cacheOrUpdateTyiotOkHttp(enableParamsCondition: Boolean) {
        val table = this
        val find =
            table.url.findTyiotCacheFromDb(table.params, table.urlId, table.lang, table.region, enableParamsCondition)
        if (find == null) {
            table.createTime = System.currentTimeMillis()
            table.updateTime = table.createTime
            table.save()
        } else {
            find.updateColumn {
                it.put("urlId", table.urlId)
                it.put("url", table.url)
                it.put("heads", table.heads)
                it.put("params", table.params)
                it.put("method", table.method)
                it.put("content", table.content)
                it.put("lang", table.lang)
                it.put("region", table.region)
                it.put("updateTime", System.currentTimeMillis())
            }
        }
    }

    /**
     * 查询缓存
     */
    open fun String.findTyiotCacheFromDb(
        params: Any? = null,
        urlId: String = KotleOkHttpCacheTable.EMPTY_URL_ID,
        lang: String? = null,
        region: String? = null,
        enableParamsCondition:Boolean = true //有的场景，param是变化的。不能作为查询条件
    ): KotleOkHttpCacheTable? {
        val map = hashMapOf<String, String>()
        val paramsJsonStr = params?.toJsonString()
        if (enableParamsCondition) {
            if (paramsJsonStr != null && paramsJsonStr != "{}") {
                map["params=?"] = paramsJsonStr
            }
        }
        if (lang != null) {
            map["lang=?"] = lang
        }
        if (region != null) {
            map["region=?"] = region
        }
        var conditionFirstStr = "url=? and urlId=?"
        val valueList = arrayListOf<String>()
        map.forEach {
            conditionFirstStr += " and ${it.key}"
            valueList.add(it.value)
        }
        val valueArray = valueList.toTypedArray<String>()
        try {
            val allResults = sqlite.where(conditionFirstStr, this, urlId, *valueArray)
            val result = allResults.findLast<KotleOkHttpCacheTable>()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 处理返回码
     * 可能会抛出IotTokenExpiredException or IotApiError
     */
    open suspend fun onHandleApiCode(
        path: String,
        params: Any?,
        code: String,
        responseJson: JSONObject,
        responseStr: String
    ): String {
        return when (code) {
            TyiotApiBean.CODE_OK -> {
                responseStr
            }

            TyiotApiBean.CODE_TOKEN_EXPIRED, TyiotApiBean.CODE_TOKEN_EXPIRED_FACTORY -> {
                if (responseJson.has("msg")) {
                    val msg = responseJson.getString("msg")
                    throw TyiotTokenExpiredException(msg)
                } else {
                    throw TyiotTokenExpiredException(responseStr)
                }
            }

            else -> {
                var da = if (responseJson.has("data")) responseJson.getString("data") else null
                if (da == "null" || da.isNullOrEmpty()) {
                    da = null
                }
                if (responseJson.has("msg")) {
                    val msg = responseJson.getString("msg")
                    throw TyiotApiError(code, msg, da)
                } else {
                    throw TyiotApiError(code, responseStr, da)
                }
            }
        }
    }

    /**
     * 如果发生改变则返回。没有发生改变则返回null
     */
    open suspend fun onHandleApiCodeTimestampErr(path: String,
                                                 params: Any?,
                                                 code: String,
                                                 responseJson: JSONObject,
                                                 responseStr: String) : Pair<String, Boolean> {
        return when (code) {
            TyiotApiBean.CODE_TIMESTAMP_ERROR -> {
                //{"code":"3","data":{"timestamp":1647084850984}}
                if (responseJson.has("data")) {//处理时间戳的偏移
                    val data = responseJson.optJSONObject("data")
                    if (data != null && data.has("timestamp")) {
                        val timestamp = data.getLong("timestamp")
                        timestampOffset = timestamp - System.currentTimeMillis()
                        Pair(path.okhttpRequest(params), true)
                    } else {
                        Pair(responseStr, false)
                    }
                } else {
                    Pair(responseStr, false)
                }
            }

            else -> Pair(responseStr, false)
        }
    }
}