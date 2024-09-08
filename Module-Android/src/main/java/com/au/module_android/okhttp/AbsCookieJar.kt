package com.au.module_android.okhttp

import androidx.annotation.Keep
import com.au.module_android.json.fromJson
import com.au.module_android.json.toJsonString
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

@Keep
class CookieStoreBean {
    var cookies: List<Cookie>? = null
    var url: String? = null
}

/**
 * cookie存储
 */
abstract class AbsCookieJar : CookieJar {
    private val cookieStoreMap = mutableMapOf<String?, CookieStoreBean>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val bean = CookieStoreBean().apply {
            this.url = url.host
            this.cookies = cookies
        }
        cookieStoreMap[host] = bean
        saveToDisk(host, bean.toJsonString())
    }

    //mmkvSave("okhttp_cookie_" + host, bean.toJsonString())
    abstract fun saveToDisk(host:String, data:String)

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        var cookieStoreBean = cookieStoreMap[host]
        if (cookieStoreBean == null) {
            loadFromDisk(host).let {
                if (it.isNotBlank()) {
                    val bean = it.fromJson<CookieStoreBean>()
                    if(bean != null) cookieStoreMap[host] = bean
                    cookieStoreBean = bean
                }
            }
        }
        return cookieStoreBean?.cookies ?: ArrayList()
    }

    //sample: mmkvGet("okhttp_cookie_" + host, "")
    abstract fun loadFromDisk(host:String) : String
}
