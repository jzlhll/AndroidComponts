package com.au.module_android.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

public final class ForeNotificationUtil {
    private static final String CHANNEL_ONE_ID = "foreNotifyId";
    private static final int NOTIFY_ID = 0x111;
    private static final int FOREGROUND_ID = 0x112;

    private static final boolean NO_SOUND = true;

    /**
     * 公开使用
     */
    public static void sendNotification(Context context, String channelName, String channelDesc, String contentTitle, String contentText) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFY_ID, getNotificationO(context, manager, channelName, channelDesc, contentTitle, contentText));
    }

    /**
     * onStartCommand调用
     */
    public static void startForeground(Service service, String channelName, String channelDesc, String contentTitle, String contentText) {
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        notification = getNotificationO(service, manager, channelName, channelDesc, contentTitle, contentText);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        service.startForeground(FOREGROUND_ID, notification);
    }

    /**
     * onDestory之前调用
     */
    public static void stopForegound(Service service) {
        service.stopForeground(true);
    }

    private static Notification getNotificationO(Context context, NotificationManager manager, String name, String desc, String contentTitle, String contentText) {
        Notification.Builder builder;
        NotificationChannel channel = null;
        channel = new NotificationChannel(CHANNEL_ONE_ID, name, NO_SOUND ? NotificationManager.IMPORTANCE_MIN : NotificationManager.IMPORTANCE_DEFAULT);
        if (NO_SOUND) {
            channel.enableVibration(false);//震动不可用
            channel.setSound(null, null); //设置没有声音
        }
        channel.setDescription(desc);
        manager.createNotificationChannel(channel);
        builder = new Notification.Builder(context, CHANNEL_ONE_ID);
        builder.setCategory(Notification.CATEGORY_RECOMMENDATION)
        .setContentTitle(contentTitle)
        .setContentText(contentText)
        //.setContentIntent(getPendingIntent(context))
        .setSmallIcon(android.R.drawable.ic_notification_overlay); //todo

        return builder.build();
        //channel.enableLights(true);
        //channel.setLightColor(color);

        //Uri mUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        //channel.setSound(mUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);

        // Register the channel with system; you can't change the importance
        // or other notification behaviors after this
    }

    //低版本
//    private static Notification getNotification(Context context, String contentTitle, String contentText) {
//        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ONE_ID)
//        .setPriority(Notification.PRIORITY_DEFAULT)
//        //.setLights(color, 1000, 0)
//        //.setSound(null, null);
//        ;
//
//        builder.setCategory(Notification.CATEGORY_RECOMMENDATION)
//        .setContentTitle(contentTitle)
//        .setContentText(contentText)
//        //.setContentIntent(getPendingIntent(context))
//        .setSmallIcon(android.R.drawable.ic_notification_overlay); //todo
//
//        return builder.build();
//    }

//    private static PendingIntent getPendingIntent(Context context) {
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return pi;
//    }

}