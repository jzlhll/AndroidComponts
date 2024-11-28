package com.au.module_android.crash

import android.os.Process
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.au.module_android.Globals
import com.au.module_android.crash.UncaughtExceptionHandlerObj.TAG
import com.au.module_android.glide.clearAppCache
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd

/**
 * @author allan
 * @date :2024/11/20 17:28
 * @description: 死在了Entry情况下，只能通过Toast给用户交互。
 */
//@Deprecated("暂时不做实现。因为需要死在entry activity的onCreate函数中，哪怕做一下post或者launch都不会触发此类白屏奔溃。")
abstract class MaybeEntryCrashedRunnable : Runnable{
    enum class CanUpdate{
        /**
         * 检查失败
         */
        CheckedFail,

        /**
         * 确定检查到了就是最新
         */
        CurrentLatest,

        /**
         * 自己下载新版本更新方式：检查到新版本
         */
        HasNewVersion,

        /**
         * 走商店升级的更新方式：检查到新版本
         */
        AppStoreHasNewVersion
    }

    companion object {
        fun create() {
            //run in main thread 因为已经entryActivity create 白屏崩溃了。不在乎卡不卡主线程了
            Toast.makeText(Globals.app, "死在了启用界面创建过程中，正在检查，请稍等...", Toast.LENGTH_LONG).show()
            ignoreError {
                val isCrashActivityOnTop = Globals.activityList.lastOrNull()?.componentName?.className == "CrashActivity"
                //证明能启动。不需要使用crashRunnable
                if (!isCrashActivityOnTop) {
                    val runnable = UncaughtExceptionHandlerObj.entryCrashedRunnableClass?.getDeclaredConstructor()?.newInstance()
                        ?: object : MaybeEntryCrashedRunnable() {
                            override fun checkAppVersion(callback: (CheckAppVersionInfo<*>) -> Unit) {
                                Thread.sleep(500)
                                callback(CheckAppVersionInfo(null, CanUpdate.CurrentLatest))
                            }

                            override fun startDownloadApp(data: Any?, progressCallback: (DownloadProgressInfo) -> Unit) {
                            }

                            override fun startInstall(filePath: String) {
                            }
                        }
                    runnable.run()
                }
            }
        }
    }

    /**
     * 自行继承，你们公司的检查数据结构放在data。
     * @param canUpdate 如果是null就表示你检查失败。true表示检查到有新版本可以升级，false表示无法升级。
     */
    open class CheckAppVersionInfo<T>(val data:T?, val canUpdate:CanUpdate)

    /**
     * 如果成功下载，就赋值filePath。换句话说，filePath != null就代表已经下载成功。
     */
    data class DownloadProgressInfo(var progress:Int, var filePath:String?)

    //为了让数据进行回调暂存。
    private class CallbackContainer {
        var checkAppInfo:CheckAppVersionInfo<*>? = null
        var downProgressInfo = DownloadProgressInfo(0, null)
    }

    private fun toastAndSleep(str:String, isLong:Boolean = true, needSleep:Boolean = true) {
        Toast.makeText(Globals.app, str, if(isLong)Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        if(needSleep) Thread.sleep(if(isLong) 3000 else 1800)
    }

    override fun run() {
        //因为这个死在entry create阶段。一直占据主线程，一直跑,Handler也不能用, mainScope也不能用。
        //只能通过toast与用户交互。所以，这里只能不断地Sleep。
        val callbackContainer = CallbackContainer()
        Thread{
            checkAppVersion { checkAppInfo->
                callbackContainer.checkAppInfo = checkAppInfo
            }
        }.start()

        var tryCount = 10
        do {
            toastAndSleep("检测到崩溃，会一直白(黑)屏，尝试检查中...")
            tryCount--
        }while(callbackContainer.checkAppInfo == null && tryCount >= 0)

        when (callbackContainer.checkAppInfo?.canUpdate) {
            null,
            CanUpdate.CheckedFail,
            CanUpdate.CurrentLatest -> {
                toastAndSleep("清理本地cache数据...", false)
                clearAppCache() //todo: clearAppFileDir()
                toastAndSleep("清理完成! 准备重启...")
                logd(TAG) { "Maybe Entry crashed kill and restart " }

                Thread.sleep(3000)
                UncaughtExceptionHandlerObj.killAndRestart(null)
            }

            CanUpdate.AppStoreHasNewVersion -> {
                toastAndSleep("应用商店有新版本，请自行去更新。")

                Thread.sleep(2000)
                Process.killProcess(Process.myPid())
                Runtime.getRuntime().exit(-1) //不能只依赖killProcess
            }

            CanUpdate.HasNewVersion -> {
                toastAndSleep("有新版本，下载0%...", needSleep = false)
                Thread{
                    startDownloadApp(callbackContainer.checkAppInfo!!.data) { downloadProgressInfo->
                        callbackContainer.downProgressInfo.progress = downloadProgressInfo.progress
                        callbackContainer.downProgressInfo.filePath = downloadProgressInfo.filePath
                    }
                }.start()

                do {
                    Thread.sleep(3000) //多等待一会儿，再check。
                    val progress = callbackContainer.downProgressInfo.progress
                    toastAndSleep("有新版本，下载$progress%...")
                }while(callbackContainer.downProgressInfo.filePath == null)

                val fp = callbackContainer.downProgressInfo.filePath
                if (fp != null) {
                    toastAndSleep("下载成功! 开始安装...")
                    startInstall(fp)
                }
            }

        }
    }

    /**
     * 是否有升级任务。如果你一次检查网络不对，请求不到，就在函数体内继续多次请求。
     * 已经帮你开启了线程。
     */
    @WorkerThread
    abstract fun checkAppVersion(callback:(CheckAppVersionInfo<*>) -> Unit)

    /**
     * 已经帮你开了子线程。
     * @param data 就是前面checkAppVersion见到的结果。回传给你。
     */
    @WorkerThread
    abstract fun startDownloadApp(data:Any?, progressCallback:(DownloadProgressInfo)->Unit)

    @WorkerThread
    abstract fun startInstall(filePath:String)
}