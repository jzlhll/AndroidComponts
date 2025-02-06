package com.au.module_android.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import androidx.annotation.DrawableRes

object ForeNotificationUtil {
    private const val CHANNEL_ONE_ID = "foreNotifyId"
    private const val NOTIFY_ID = 0x111
    private const val FOREGROUND_ID = 0x112

    private const val NO_SOUND = true

    /**
     * 公开使用
     */
    fun sendNotification(context: Context, channelName: String, channelDesc: String, contentTitle: String, contentText: String, @DrawableRes noBgIcon:Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFY_ID, getNotificationO(context, manager, channelName, channelDesc, contentTitle, contentText, noBgIcon, null))
    }

    /**
     * onStartCommand调用
     */
    @JvmStatic
    fun startForeground(service: Service, channelName: String, channelDesc: String, contentTitle: String, contentText: String,
                        @DrawableRes noBgIcon:Int? = null,
                        pendingIntent: PendingIntent? = null) {
        val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = getNotificationO(service, manager, channelName, channelDesc, contentTitle, contentText, noBgIcon, pendingIntent)
        notification.flags = Notification.FLAG_ONGOING_EVENT
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        service.startForeground(FOREGROUND_ID, notification)
    }

    /**
     * onDestory之前调用
     */
    @JvmStatic
    fun stopForeground(service: Service) {
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun getNotificationO(
        context: Context,
        manager: NotificationManager,
        name: String,
        desc: String,
        contentTitle: String,
        contentText: String,
        @DrawableRes noBgIcon:Int?,
        pendingIntent: PendingIntent?
    ): Notification {
        var channel: NotificationChannel? = null
        channel = NotificationChannel(CHANNEL_ONE_ID, name, if (NO_SOUND) NotificationManager.IMPORTANCE_MIN else NotificationManager.IMPORTANCE_DEFAULT)
        if (NO_SOUND) {
            channel.enableVibration(false) //震动不可用
            channel.setSound(null, null) //设置没有声音
        }
        channel.description = desc
        manager.createNotificationChannel(channel)
        val builder = Notification.Builder(context, CHANNEL_ONE_ID)
        builder.setCategory(Notification.CATEGORY_RECOMMENDATION)
            .setContentTitle(contentTitle)
            .setContentText(contentText) //.setContentIntent(getPendingIntent(context))
        if (noBgIcon != null) {
            builder.setSmallIcon(noBgIcon)
        }
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()

        //others:
        //channel.enableLights(true);
        //channel.setLightColor(color);

        //Uri mUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        //channel.setSound(mUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);

        // Register the channel with system; you can't change the importance
        // or other notification behaviors after this
    }
}