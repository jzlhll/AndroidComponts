package com.au.jobstudy

import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.star.StarConsts
import com.au.module_cached.AppDataStore
import com.au.module_android.Globals
import com.au.module_android.init.GlobalBackgroundCallback
import com.au.module_android.InitApplication
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_okhttp.OkhttpGlobal
import com.au.module_okhttp.beans.OkhttpInitParams
import com.au.module_okhttp.creator.AbsCookieJar

/**
 * @author au
 * @date :2023/11/14 14:05
 * @description:
 */
class MyInitApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()

        OkhttpGlobal.initParams(OkhttpInitParams().also {
            it.okHttpCookieJar = object : AbsCookieJar() {
                override fun saveToDisk(host: String, data: String) {
                    AppDataStore.save("okhttp_cookie_$host", data)
                }

                override fun loadFromDisk(host: String): String {
                    return AppDataStore.readBlocked("okhttp_cookie_$host", "")
                }
            }
        })

        GlobalBackgroundCallback.addListener {
            logd { "update SummerConst when foreground $it" }
            if (!it) {
                Globals.mainScope.launchOnThread {
                    StarConsts.onlyInitOnce()
                    CheckConsts.whenTrigger()
                }
            }
        }
    }
}