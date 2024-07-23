package com.au.module_android.utils

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.permissions.createPermissionForResult
import com.au.module_android.permissions.permission.IOnePermissionResult

class NotificationUtil(private val context:Context = Globals.app) {
    companion object {
        private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
        /**
         * 在适当的时机调用弹出系统请求权限。android12以下不需要调用。
         * @param permissionHelper 通过createPostNotificationPermissionResult创建而来。
         */
        fun onceRequestPermission(permissionHelper: IOnePermissionResult) { //可以做下cache保存。避免多次请求。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notify = NotificationUtil()
                if ((!notify.isEnabled() || !notify.isCanNotify)) {
                    notify.safeRun(permissionHelper) {
                        logd { "Notification Util permission run success." }
                    }
                }
            }
        }

        fun createPostNotificationPermissionResult(lifeOwner:LifecycleOwner) =
            lifeOwner.createPermissionForResult(POST_NOTIFICATIONS)
    }

    private val notificationMgr:NotificationManagerCompat = NotificationManagerCompat.from(context)

    /**
    @description 检测权限列表是否授权，如果未授权，遍历请求授权
     */
    fun safeRun(
        permissionHelper: IOnePermissionResult,
        block: () -> Unit
    ) {
        permissionHelper.safeRun(block)
    }

    /**
     * 发送通知
     */
    fun notification(
        id: Int,/*通知id，唯一*/
        notification: Notification
    ) {
        //发送通知
        val isEnabled = isEnabled()
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            && isEnabled) {
            notificationMgr.notify(id, notification)
        }
    }

    val isCanNotify:Boolean
        get() = (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    /**
     * 发送简单文本消息
     */
    fun notificationSimpleText(
        id: Int,/*通知id，唯一*/
        channelId: String,/*通知渠道id*/
        content: String,
        @DrawableRes smallIcon: Int,
        @DrawableRes largeIcon: Int? = null,
        title:String? = null,
        importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        jumpMainAndClearNotification:Boolean = false,
        jumpActivityClass:Class<*>? = null,
        channelBuildAction: ((NotificationChannelCompat.Builder) -> Unit) = {},
        notificationBuildAction: ((NotificationCompat.Builder) -> Unit) = {},/*构建通知的额外参数*/
    ) {
        notification(id, channelId, smallIcon, largeIcon = largeIcon, importance,
            jumpMainAndClearNotification = jumpMainAndClearNotification,
            jumpActivityClass = jumpActivityClass,
            channelBuildAction,
             notificationBuildAction = {
                it.setContentText(content)
                if (title != null) it.setContentTitle(title)
                notificationBuildAction.invoke(it)
            })
    }

    /**
     * 发送一个通知,
     * 发送通知前必须先创建对应的渠道
     */
    fun notification(
        id: Int,/*通知id，唯一*/
        channelId: String,/*通知渠道id*/
        @DrawableRes smallIcon: Int,/*logo，不传报错*/
        @DrawableRes largeIcon: Int? = null,/*logo，不传报错*/
        importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        jumpMainAndClearNotification:Boolean = false,
        jumpActivityClass:Class<*>? = null,
        channelBuildAction: ((NotificationChannelCompat.Builder) -> Unit) = {},
        notificationBuildAction: (NotificationCompat.Builder) -> Unit,/*构建通知的额外参数*/
    ) {
        if (notificationGetChannel(channelId) == null) {
            notificationChannel(channelId, channelId, importance, channelBuildAction)
        }
        //构建通知
        val notification = notificationBuilder(channelId, smallIcon, largeIcon)
            .also {
                notificationBuildAction.invoke(it)
                if (jumpMainAndClearNotification) {
                    val intent = Intent(Globals.app, jumpActivityClass)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    val pendingIntent = PendingIntent.getActivity(Globals.app,0, intent, PendingIntent.FLAG_IMMUTABLE)
                    it.setContentIntent(pendingIntent)
                }
            }.build()

        if (jumpMainAndClearNotification) {
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        }
        //发送通知
        notification(id, notification)
    }

    /**
     * 创建一个通知渠道
     */
    fun notificationBuilder(
        channelId: String,/*通知渠道*/
        @DrawableRes smallIcon: Int,/*logo，不传报错*/
        @DrawableRes largeIcon: Int?,
        importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        channelBuildAction: ((NotificationChannelCompat.Builder) -> Unit) = {},
    ): NotificationCompat.Builder {
        if (notificationGetChannel(channelId) == null) {
            notificationChannel(channelId, channelId, importance, channelBuildAction)
        }
        //构建通知
        val build = NotificationCompat.Builder(context, channelId)
            //使用默认的声音、振动、闪光
            .setSmallIcon(smallIcon)
        if (largeIcon != null) {
            build.setLargeIcon(BitmapFactory.decodeResource(Globals.app.resources, largeIcon))
        }
        return build
    }


    /**
     * 创建渠道
     */
    fun notificationChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
        channelBuildAction: ((NotificationChannelCompat.Builder) -> Unit) = {},/*渠道构建的额外参数*/
    ) {
        val channel = NotificationChannelCompat.Builder(channelId, importance)
            .setName(channelName)
            .also {
                channelBuildAction.invoke(it)
            }.build()
        notificationMgr.createNotificationChannel(channel)
    }

    /**
     * 创建渠道组
     */
    fun notificationChannelGroup(
        groupId: String,
        groupName: String,
        channelBuildAction: ((NotificationChannelGroupCompat.Builder) -> Unit) = {},/*渠道构建的额外参数*/
    ) {
        val channel = NotificationChannelGroupCompat.Builder(
            groupId
        ).setName(groupName)
            .also {
                channelBuildAction.invoke(it)
            }.build()
        notificationMgr.createNotificationChannelGroup(channel)
    }

    /**
     * 移除通知
     */
    fun notificationCancel(id: Int) {
        notificationMgr.cancel(id)
    }

    /**
     * 移除所有通知
     */
    fun notificationCancelAll() {
        notificationMgr.cancelAll()
    }

    /**
     * 删除渠道
     */
    fun notificationDeleteChannel(channelId: String) {
        notificationMgr.deleteNotificationChannel(channelId)
    }

    /**
     * 删除渠道组
     */
    fun notificationDeleteChannelGroup(groupId: String) {
        notificationMgr.deleteNotificationChannelGroup(groupId)
    }

    /**
     * 通知是否可用
     */
    fun isEnabled() =
        notificationMgr.areNotificationsEnabled()

    /**
     * 获取创建的渠道
     */
    fun notificationGetChannel(channelId: String) =
        notificationMgr.getNotificationChannelCompat(channelId)

    /**
     * 获取创建的渠道组
     */
    fun notificationGetChannelGroup(groupId: String) =
        notificationMgr.getNotificationChannelGroupCompat(groupId)
}