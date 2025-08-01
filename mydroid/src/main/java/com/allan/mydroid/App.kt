package com.allan.mydroid

import com.allan.mydroid.api.Api
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.globals.cacheImportCopyDir
import com.au.logsystem.DefaultActivitiesFollowCallback
import com.au.module_android.Globals
import com.au.module_android.InitApplication
import com.au.module_android.utils.clearDirOldFiles
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.logd
import com.au.module_androidui.toast.ToastBuilder
import com.au.module_cached.AppDataStore
import com.au.module_okhttp.OkhttpGlobal
import com.au.module_okhttp.beans.OkhttpInitParams
import com.au.module_okhttp.creator.AbsCookieJar
import com.au.module_okhttp.interceptors.PretreatmentInterceptor
import com.au.module_okhttp.interceptors.SimpleRetryInterceptor
import com.modulenative.AppNative
import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
class App : InitApplication() {
    override fun initBeforeAttachBaseContext() {
    }
    override fun onCreate() {
        super.onCreate()
        OkhttpGlobal.initBeforeAnyRequest(OkhttpInitParams().also {
            it.okHttpCookieJar = object : AbsCookieJar() {
                override fun saveToDisk(host: String, data: String) {
                    AppDataStore.save("okhttp_cookie_$host", data)
                }

                override fun loadFromDisk(host: String): String {
                    return AppDataStore.readBlocked("okhttp_cookie_$host", "")
                }
            }

            it.okhttpExtraBuilder = { builder->
                builder.addInterceptor(SimpleRetryInterceptor(
                    headersResetBlock = { request->
                        request //填充。更改request的部分参数，比如时间戳等信息
                    },
                    timestampOffsetBlock = { timestampOffset->
                        Api.timestampOffset = timestampOffset
                        //填充。将timestampOffset进行存储。用于后续请求传参使用。
                    },
                    tokenExpiredBlock = { msg->
                        ToastBuilder().setMessage(msg).setOnTopLater().toast()
                        //填充。仅仅是一个提醒。tokenExpire过期的时候，给出一个全局的通知。具体的那个请求还是抛异常。
                    }
                ))
                builder.addInterceptor(PretreatmentInterceptor())
            }
        })

        registerActivityLifecycleCallbacks(MyDroidGlobalService)
        registerActivityLifecycleCallbacks(DefaultActivitiesFollowCallback())

        //一上来直接强制移除所有临时import的文件。
        Globals.mainScope.launchOnIOThread {
            logd { "alland delayed" }
            AppNative.strEk(this@App)
            clearDirOldFiles(cacheImportCopyDir(), 0)

            delay(5000)
            logd { "alland delayed atsf 5s" }
            //AppNative.astf(this@App, "device_test.zip", Globals.goodCacheDir.absolutePath + "/cached.zip")
            logd { "alland delayed atsf success!" }
        }
    }
}