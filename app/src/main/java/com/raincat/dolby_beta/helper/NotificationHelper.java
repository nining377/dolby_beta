package com.raincat.dolby_beta.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
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
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void sendUnLockNotification(Context context, int appId, String ticker, String title, String content, int resId) {
        sendUnLockNotification(context, appId, true, true, ticker, title, content, resId, null, null);
    }

    /**
     * 设置一个不常驻通知栏的Notification
     *
     * @param appId     标识符
     * @param isVibrate 是否震动
     * @param isSound   是否发声
     * @param ticker    通知首次出现在通知栏时提醒的文字
     * @param title     标题
     * @param content   详细内容
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendUnLockNotification(Context context, int appId, boolean isVibrate, boolean isSound, String ticker, String title, String content, int resId) {
        sendUnLockNotification(context, appId, isVibrate, isSound, ticker, title, content, resId, null, null);
    }

    /**
     * 设置一个不常驻通知栏的Notification，带点击跳转效果
     *
     * @param appId     标识符
     * @param ticker    通知首次出现在通知栏时提醒的文字
     * @param title     标题
     * @param content   详细内容
     * @param className 需要跳转的Activity名字
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendUnLockNotification(Context context, int appId, String ticker, String title, String content, int resId, String className) {
        sendUnLockNotification(context, appId, true, true, ticker, title, content, resId, className, null);
    }

    /**
     * 设置一个不常驻通知栏的Notification，带点击跳转效果
     *
     * @param appId     标识符
     * @param isVibrate 是否震动
     * @param isSound   是否发声
     * @param ticker    通知首次出现在通知栏时提醒的文字
     * @param title     标题
     * @param content   详细内容
     * @param className 需要跳转的Activity名字
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendUnLockNotification(Context context, int appId, boolean isVibrate, boolean isSound, String ticker, String title, String content, int resId, String className) {
        sendUnLockNotification(context, appId, isVibrate, isSound, ticker, title, content, resId, className, null);
    }

    /**
     * 设置一个不常驻通知栏的Notification，带点击跳转效果，带传参
     *
     * @param appId     标识符
     * @param isVibrate 是否震动
     * @param isSound   是否发声
     * @param ticker    通知首次出现在通知栏时提醒的文字
     * @param title     标题
     * @param content   详细内容
     * @param className 需要跳转的Activity名字
     * @param param     往跳转的Activity传参
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendUnLockNotification(Context context, int appId, boolean isVibrate, boolean isSound, String ticker, String title, String content, int resId, String className, HashMap<String, String> param) {
        Notification.Builder builder = new Notification.Builder(context);
        if (resId != 0)
            builder.setSmallIcon(resId);
        else {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            Drawable drawable = applicationInfo.loadIcon(context.getPackageManager());
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            Icon icon = Icon.createWithBitmap(bitmap);
            builder.setSmallIcon(icon);
        }
        builder.setContentTitle(title);
        builder.setTicker(ticker);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName() + appId, "通知", NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId(context.getPackageName() + appId);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(isVibrate);
            notificationChannel.setVibrationPattern(new long[]{200L, 200L, 200L, 200L});
            if (isSound) {
                notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
            } else {
                notificationChannel.setSound(null, null);
            }

            deleteNoNumberNotification(this.mNotificationManager, "UnblockNeteaseMusic");
            mNotificationManager.createNotificationChannel(notificationChannel);
        } else {
            if (isVibrate) {
                builder.setVibrate(new long[]{200L, 200L, 200L, 200L});
            }

            if (isSound) {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        }

        if (className != null) {
            Intent appIntent = new Intent(Intent.ACTION_MAIN);
            appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            appIntent.setComponent(new ComponentName(context.getPackageName(), context.getPackageName() + "." + className));
            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (param != null)
                for (Map.Entry<String, String> p : param.entrySet()) {
                    appIntent.putExtra(p.getKey(), p.getValue());
                }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        } else {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(), PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(pendingIntent);
        }

        Notification notification = new Notification.BigTextStyle(builder)
                .bigText(content).build();
        this.mNotificationManager.notify(appId, notification);
    }

    @RequiresApi(api = 26)
    private void deleteNoNumberNotification(NotificationManager nm, String newChannelId) {
        List<NotificationChannel> notificationChannels = nm.getNotificationChannels();
        if (notificationChannels != null && notificationChannels.size() != 0) {

            for (NotificationChannel channel : notificationChannels) {
                if (channel.getId() != null && !channel.getId().equals(newChannelId)) {
                    int notificationNumbers = this.getNotificationNumbers(nm, channel.getId());
                    if (notificationNumbers == 0) {
                        nm.deleteNotificationChannel(channel.getId());
                    }
                }
            }

        }
    }

    @RequiresApi(api = 26)
    private int getNotificationNumbers(NotificationManager mNotificationManager, String channelId) {
        if (mNotificationManager != null && !TextUtils.isEmpty(channelId)) {
            int numbers = 0;
            StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();
            int length = activeNotifications.length;

            for (StatusBarNotification item : activeNotifications) {
                Notification notification = item.getNotification();
                if (notification != null && channelId.equals(notification.getChannelId())) {
                    ++numbers;
                }
            }

            return numbers;
        } else {
            return -1;
        }
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
