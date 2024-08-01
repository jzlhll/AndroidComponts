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
import com.au.module_android.utils.dp

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
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 60.dp))
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
        channel.description = "notifyChannelName"
        notificationManager.createNotificationChannel(channel)

// Get the layouts to use in the custom notification.
        val notificationLayout = RemoteViews(requireActivity().packageName, R.layout.notify_small)
        notificationLayout.setTextViewText(R.id.notification_desc, "This a long long long long long long long long long long long long desc.")
        val notificationLayoutExpanded = RemoteViews(requireActivity().packageName, R.layout.notify_big)

        val intent1 = Intent(Globals.app, EntroActivity::class.java).also { it.putExtra("goto", "LiveData") }
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val clickIntent1 = PendingIntent.getActivity(Globals.app,1, intent1, PendingIntent.FLAG_IMMUTABLE)

        val intent2 = Intent(Globals.app, EntroActivity::class.java).also { it.putExtra("goto", "FontTest") }
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val clickIntent2 = PendingIntent.getActivity(Globals.app,2, intent2, PendingIntent.FLAG_IMMUTABLE)

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.gotoBtn, clickIntent2)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.ic_failure, clickIntent1)

        notificationLayoutExpanded.setTextViewText(R.id.notification_title, "new title")
        val intent = Intent(Globals.app, EntroActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(Globals.app,0, intent, PendingIntent.FLAG_IMMUTABLE) //所有的intent，requstCode必须不同。

// Apply the layouts to the notification.
        val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("hahah") //不同手机不一定有显示
            .setStyle(NotificationCompat.BigTextStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded) //华为手机不论Big不bigContentView，都需要自行展开。
            .setPriority(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false) //是否点击整条消失
            .build()

        notificationManager.notify(666, customNotification)
    }
}