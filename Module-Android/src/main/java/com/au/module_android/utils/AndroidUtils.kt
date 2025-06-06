package com.au.module_android.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Dialog
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.au.module_android.Globals
import com.au.module_android.Globals.app
import com.au.module_android.ui.FragmentShellActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.roundToInt

val isMainThread: Boolean
    get() = Looper.getMainLooper() === Looper.myLooper()

/**
 * 类型转换
 */
inline fun <reified T> Any?.asOrNull(): T? = this as? T

val View.activity: Activity?
    get() {
        val ctx = context
        if (ctx is Activity) {
            return ctx
        }
        if (ctx is ContextWrapper) {
            return ctx.baseContext.asOrNull()
        }
        return null
    }

/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Activity.dp(value:Float):Float {
    return value * this.resources.displayMetrics.density
}

/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Fragment.dp(value:Float):Float {
    return value * requireActivity().resources.displayMetrics.density
}
/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Dialog.dp(value:Float):Float {
    return value * this.context.resources.displayMetrics.density
}

fun Context.dp(value:Float) : Float {
    if (this is Activity) {
        return this.dp(value)
    }

    return value.dp
}

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Float.dp:Float
    get() = (this * app.resources.displayMetrics.density)

val Float.dpInt:Int
    get() = (this * app.resources.displayMetrics.density).roundToInt()

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Int.dp:Int
    get() = (this.toFloat() * app.resources.displayMetrics.density).roundToInt()

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Int.dpFloat:Float
    get() = this.toFloat() * app.resources.displayMetrics.density

/**
 * 获取缓存大小
 */
suspend fun getAppCacheSize(): String {
    return withIoThread {
        val cacheDirSz = app.cacheDir.getDirSize()
        val externalCacheDirSz = app.externalCacheDir.getDirSize()
        (cacheDirSz + externalCacheDirSz).formatLength()
    }
}

/**
 * 字节转为kb
 */
fun Long?.formatLength(): String {
    val size = this?.toFloat() ?: return "0MB"
    return when {
        size < 1024 * 1024 * 1024 -> {//不足1g
            "${(size / 1024 / 1024).keepTwoPoint()}MB"
        }
        else -> {
            "${(size / 1024 / 1024 / 1024).keepTwoPoint()}GB"
        }
    }
}

/**
 * 保留两位小数
 */
fun Float?.keepTwoPoint(roundingMode: RoundingMode = RoundingMode.HALF_EVEN): String {
    this ?: return "0.00"
    return this.toString().keepTwoPoint()
}

/**
 * 保留两位小数
 */
fun String?.keepTwoPoint(roundingMode: RoundingMode = RoundingMode.HALF_EVEN): String {
    return try {
        if (this == null) {
            "0.00"
        } else {
            BigDecimal(this).setScale(2, roundingMode)?.toString() ?: this
        }
    } catch (e: Throwable) {
        "0.00"
    }
}

private fun isAppForeground(context: Context) : Boolean {
    val keyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (keyguardManager.inKeyguardRestrictedInputMode()) {
        return false // Screen is off or lock screen is showing
    }

    // Screen is on and unlocked, now check if the process is in the foreground
    if (!(VERSION.SDK_INT >= 21)) {
        // Before L the process has IMPORTANCE_FOREGROUND while it executes BroadcastReceivers.
        // As soon as the service is started the BroadcastReceiver should stop.
        // UNFORTUNATELY the system might not have had the time to downgrade the process
        // (this is happening consistently in JellyBean).
        // With SystemClock.sleep(10) we tell the system to give a little bit more of CPU
        // to the main thread (this code is executing on a secondary thread) allowing the
        // BroadcastReceiver to exit the onReceive() method and downgrade the process priority.
        SystemClock.sleep(10)
    }
    val pid = Process.myPid()
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = am.runningAppProcesses
    if (appProcesses != null) {
        for (process in appProcesses) {
            if (process.pid == pid) {
                return process.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
    }
    return false
}


/**
 *
 * 未知activity，打开一个packageName的应用。
 * 如果是android11需要添加可见性：在androidManifest中申明:
 * <code>
 *
 *  <queries>
 *     <!-- Specific apps you interact with, eg: -->
 *     <package android:name="com.example.store" />
 *     <package android:name="com.example.service" />
 *
 *     <!--
 *     Specific intents you query for,
 *     eg: for a custom share UI
 *     -->
 *     <intent>
 *     <action android:name="android.intent.action.SEND" />
 *     <data android:mimeType="image/jpeg" />
 *     </intent>
 *  </queries>
 *
 * </code>
 */
fun openApp(context: Context, packageName: String) : Boolean{
    try {
        val pm = context.packageManager
        val pi: PackageInfo = pm.getPackageInfo(packageName, 0)
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resolveIntent.setPackage(pi.packageName)

        val apps: List<ResolveInfo> = pm.queryIntentActivities(resolveIntent, 0)

        val ri = apps.iterator().next()
        val cn = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setComponent(cn)
        context.startActivityFix(intent)
        return true
    } catch (e:Exception) {
        e.printStackTrace()
        return false
    }
}

fun openApp2(context: Context, packageName: String) : Boolean{
    try {
        val appIntent = getAppIntent(context, packageName) ?: return false
        val className = appIntent.component?.className ?: return false
        val cn = ComponentName(packageName, className)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setComponent(cn)
        context.startActivityFix(intent)
        return true
    } catch (e:Exception) {
        e.printStackTrace()
        return false
    }
}

fun openUrlByBrowser(url: String, context: Context) {
    if (url.isNotBlank()) {
        ignoreError {
            val intent = Intent()
            intent.setAction("android.intent.action.VIEW")
            val cvtUrl = URLDecoder.decode(url, "utf-8")
            intent.setData(Uri.parse(cvtUrl))
            context.startActivityFix(intent)
        }
    }
}

/**
 * 获取打开其他app的intent
 */
fun getAppIntent(context:Context, packageName: String): Intent? {
    return context.packageManager.getLaunchIntentForPackage(packageName)
}

fun openAppActivity(context: Context, packageName: String, activityName:String) : Boolean{
    try {
        val intent = Intent()
        intent.setComponent(ComponentName(packageName, activityName))
        context.startActivityFix(intent)
        return true
    } catch (e:Exception) {
        e.printStackTrace()
        return false
    }
}

/**
 * 兼容android13+的广播注册，避免报错
 */
fun Context.registerReceiverFix(receiver: BroadcastReceiver, filter: IntentFilter,
                                receiverSystemOrOtherApp:Boolean = true) {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(
            receiver, filter,
            if (receiverSystemOrOtherApp) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        )
    } else {
        registerReceiver(receiver, filter)
    }
}

fun Intent.iteratorPrint(tag:String = TAG) {
    extras?.iteratorPrint(tag)
}

fun Bundle.iteratorPrint(tag:String = TAG) {
    keySet()?.forEach {
        Log.d(tag, "key: " + it + ", value: " + this.get(it))
    }
}

/**
 * 对字符串进行MD5加密
 *
 * @return 计算出的MD5哈希值的十六进制字符串表示，如果计算失败则返回空字符串
 */
fun String.md5(): String {
    try {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        val digest: ByteArray = instance.digest(this.toByteArray())
        val sb = StringBuilder()
        for (b in digest) {
            val hexString = Integer.toHexString(b.toInt() and 0xff)
            if (hexString.length < 2) {
                sb.append("0")
            }
            sb.append(hexString)
        }
        return sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        //do nothing.
        return ""
    }
}

/**
 * 找到启动的activity
 * first是启动的activity的Intent。
 * second如果是true就找到了。false就是没找到。
 */
fun findLaunchActivity(context: Context): Pair<Intent, Boolean> {
    val l = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
    val className = l.component?.className
    val found = Globals.activityList.find { className?.contains(it.javaClass.simpleName) == true}
    return l to (found != null)
}

/**
 * 找到是否已经存在的某个activity
 */
fun findActivity(activityCls: Class<*>): Boolean {
    val found = Globals.activityList.find { it.javaClass == activityCls}
    return found != null
}

/**
 * 如果是我们框架的代码，则可以用Fragment的来判断
 */
fun findCustomFragmentGetActivity(customFragment: Class<*>): Activity? {
    val found = Globals.activityList.find {
        it.javaClass == FragmentShellActivity::class.java && (it as FragmentShellActivity).fragmentClass == customFragment
    }
    return found
}