package com.au.module_android.glide

import com.bumptech.glide.load.model.GlideUrl

/**
 * @author au
 * Date: 2023/2/21
 * https://blog.csdn.net/qq_33635618/article/details/103479064
 * Description 通过使用自定义GlideUrl限定时间，来将glide做区分。
 *
 * https://blog.csdn.net/ZYJWR/article/details/93530341
 * later：具体某一个请求，也可以设置signature来解决。
 *
 * 粗略的实现：目前保证cache时间为5-13天。
 */
class LimitTimeGlideUrl(private val url: String?) : GlideUrl(url) {
    companion object {
        const val CACHE_MAX_TIME = 1000L * 3600 * 24 * 8
        const val UP_TO_NEXT_PERIOD = 1000L * 3600 * 24 * 5
    }

    override fun getCacheKey(): String {
        val origKey = super.getCacheKey()
        val cur = System.currentTimeMillis()
        var period = cur / CACHE_MAX_TIME
        //如果当前时间，已经接近下一个时间阶段，则进位。否则，靠近下一个阶段则太容易过期了。
        if(cur % CACHE_MAX_TIME > UP_TO_NEXT_PERIOD) period+=1
        return origKey + period

        //如果图片包含了问号结尾，则可以返回重新定义的url
//        val url = this.url
//        return if (url != null && url.contains("?")) {
//            url.substring(0, url.lastIndexOf("?"))
//        } else {
//
//        }
    }
}