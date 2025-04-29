package com.allan.autoclickfloat.activities.autofs.spider.filter

abstract class WebViewFilterBase {
    companion object {
        fun create(type: UrlSupportType) : WebViewFilterBase {
            return when (type) {
                UrlSupportType.SPIDER -> SpiderWebViewFilter()
                UrlSupportType.WEBVIEW -> NormalWebViewFilter()
                UrlSupportType.NONE -> NonWebViewFilter()
            }
        }
    }

    fun getFilterType() : UrlSupportType {
        return when (this) {
            is SpiderWebViewFilter -> UrlSupportType.SPIDER
            is NormalWebViewFilter -> UrlSupportType.WEBVIEW
            else -> UrlSupportType.NONE
        }
    }

    abstract fun isUrlSupport(url:String?) : Boolean
}

