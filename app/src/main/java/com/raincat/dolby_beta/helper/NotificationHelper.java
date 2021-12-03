package com.raincat.dolby_beta.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * 一个通用的Notification工具
 * <p>
 * Created by Administrator on 2018/3/29 0029.
 */

public class NotificationHelper {
    private NotificationManager mNotificationManager;
    private static NotificationHelper mNotificationHelper;

    public static NotificationHelper getInstance(Context context) {
        if (mNotificationHelper == null) {
            mNotificationHelper = new NotificationHelper(context);
        }
        return mNotificationHelper;
    }

    private NotificationHelper(Context context) {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 设置一个不常驻通知栏的Notification
     *
     * @param appId   标识符
     * @param ticker  通知首次出现在通知栏时提醒的文字
     * @param title   标题
     * @param content 详细内容
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendUnLockNotification(Context context, int appId, String ticker, String title, String content) {
        Notification.Builder builder = new Notification.Builder(context);
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        Drawable drawable = applicationInfo.loadIcon(context.getPackageManager());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        Icon icon = Icon.createWithBitmap(bitmap);
        builder.setSmallIcon(icon)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .setContentTitle(title)
                .setTicker(ticker)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName() + appId, "UnblockNeteaseMusic", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{200L, 200L, 200L, 200L});
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
            builder.setChannelId(context.getPackageName() + appId);
            mNotificationManager.createNotificationChannel(notificationChannel);
        } else {
            builder.setVibrate(new long[]{200L, 200L, 200L, 200L});
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        Notification notification = new Notification.BigTextStyle(builder).bigText(content).build();
        mNotificationManager.notify(appId, notification);
    }

    /**
     * 取消该通知
     *
     * @param appId 通知标识符
     */
    public void cancelNotification(int appId) {
        mNotificationManager.cancel(appId);
    }
}
