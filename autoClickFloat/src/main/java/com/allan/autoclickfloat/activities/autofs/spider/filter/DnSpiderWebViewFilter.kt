package com.allan.autoclickfloat.activities.autofs.spider.filter

class DnSpiderWebViewFilter : WebViewFilterBase() {
    override fun isUrlSupport(url: String?) : Boolean {
        url ?: return true
        for (str in blockContainsList) {
            if (url.contains(str)) {
                return false
            }
        }

        for (str in blockEndList) {
            if (url.contains(str)) {
                return false
            }
        }

        for (strArr in blockAllContainsList) {
            var isContainsAll = true
            for (str in strArr) {
                if (!url.contains(str)) {
                    isContainsAll = false
                    break
                }
            }
            if (isContainsAll) {
                return false
            }
        }
        return true
    }

    private val blockEndList = listOf(".png", ".jpg", ",jpeg", ".ico", ".js")
    private val blockContainsList = listOf("baidu",
        "openinstall",
        //"activity/report",
        "toutiao",
        "push.js",
        "wx.qq.",
        "mini-app",
        //"pc_wap",
        "search_hot_word",
        "fingerprintjs",

        "captcha",
        "bs_bot",
        //"previewImage",

        //最关键的就是它，有弹窗。
        "detail_enter",

        //trying
        "toolbar"
        //"pc_wap_commontools"
    )

    private val blockAllContainsList = listOf(
        arrayOf("dnimg", "login"),
    )

}