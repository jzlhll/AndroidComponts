package com.allan.androidlearning.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.allan.androidlearning.EntroActivity
import com.allan.androidlearning.R
import com.allan.classnameanno.EntroFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.NotificationUtil

/**
 * @author allan
 * @date :2024/7/18 11:01
 * @description:
 */
@EntroFrgName
class NotifyFragment : ViewFragment() {
    companion object {
        val CHANNEL_ID = "test_notify_chan_id"
        val NO_SOUND = false
    }

    val permissionUtil = NotificationUtil.createPostNotificationPermissionResult(this)

    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        NotificationUtil.onceRequestPermission(permissionUtil)

        return LinearLayout(inflater.context).also {
            it.addView(Button(inflater.context).also {
                it.text = "通知啊"
                it.onClick {
                    Globals.mainHandler.postDelayed({sendNotif()}, 3000)
                }
            })
        }
    }

    fun sendNotif() {
        val context = requireActivity()
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel: NotificationChannel? = null
        channel = NotificationChannel(CHANNEL_ID, "notify name", if (NO_SOUND) NotificationManager.IMPORTANCE_MIN else NotificationManager.IMPORTANCE_DEFAULT)
        if (NO_SOUND) {
            channel.enableVibration(false) //震动不可用
            channel.setSound(null, null) //设置没有声音
        }
        channel.description = "notify desc"
        notificationManager.createNotificationChannel(channel)

// Get the layouts to use in the custom notification.
        val notificationLayout = RemoteViews(requireActivity().packageName, R.layout.notify_small)
        val notificationLayoutExpanded = RemoteViews(requireActivity().packageName, R.layout.notify_big)
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, "new title")
        val intent = Intent(Globals.app, EntroActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(Globals.app,0, intent, PendingIntent.FLAG_IMMUTABLE)

// Apply the layouts to the notification.
        val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.au.module.android.R.drawable.ic_warning)
            .setStyle(NotificationCompat.BigTextStyle())
//            .setCustomContentView(notificationLayout)
            .setCustomContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(666, customNotification)
    }
}