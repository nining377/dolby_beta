package com.raincat.dolby_beta.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/09/08
 *     desc   : 工具类
 *     version: 1.0
 * </pre>
 */

public class Tools {
    /**
     * 获取线程名称
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return "";
    }

    /**
     * 吐司
     */
    public static void showToastOnLooper(final Context context, final String message) {
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dp2px
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取今天0点的时间戳
     */
    public static long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime();
    }

    /**
     * ADB命令
     */
    public static void shell(Command command) {
        try {
            RootTools.closeAllShells();
            RootTools.getShell(false).add(command);
        } catch (TimeoutException | RootDeniedException | IOException e) {
            e.printStackTrace();
        }
    }
}