package com.allan.autoclickfloat.activities.autofs.spider.filter

class NonWebViewFilter : WebViewFilterBase() {
    override fun isUrlSupport(url: String?) : Boolean {
        return true
    }
}