package com.allan.autoclickfloat.activities.autofs.spider.filter

class SpiderWebViewFilter : WebViewFilterBase() {
    override fun isUrlSupport(url: String?) : Boolean {
        url ?: return true
        return when {
            url.contains("csdnimg") -> false
            else -> true
        }
    }
}