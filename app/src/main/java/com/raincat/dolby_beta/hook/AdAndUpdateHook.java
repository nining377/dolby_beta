package com.raincat.dolby_beta.hook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 去广告和升级
 *     version: 1.0
 * </pre>
 */

public class AdAndUpdateHook {
    private static String newCallMethodString = "newCall";
    private static String httpUrlFieldString = "url";
    private static String urlFieldString = "url";

    public AdAndUpdateHook(Context context) {
        //去广告和升级
        Class<?> okHttpClientClass = findClassIfExists("okhttp3.OkHttpClient", context.getClassLoader());
        if (okHttpClientClass == null) {
            okHttpClientClass = findClassIfExists("okhttp3.x", context.getClassLoader());
            newCallMethodString = "a";
            httpUrlFieldString = "a";
            urlFieldString = "j";
        }

        if (okHttpClientClass != null)
            hookAllMethods(okHttpClientClass, newCallMethodString, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args != null && param.args.length == 1 && param.args[0].getClass().getName().contains("okhttp")) {
                        Object request = param.args[0];
                        Field httpUrl = request.getClass().getDeclaredField(httpUrlFieldString);
                        httpUrl.setAccessible(true);
                        Object urlObj = httpUrl.get(request);
                        //加了一个反营销版权保护的URL，暂时作用未知
                        if (urlObj.toString().contains("appcustomconfig/get") || (SettingHelper.getInstance().isEnable(SettingHelper.black_key) && !urlObj.toString().contains("music.126.net") && (urlObj.toString().contains("api/ad") || urlObj.toString().endsWith(".jpg") || urlObj.toString().endsWith(".mp4")))
                                || (SettingHelper.getInstance().isEnable(SettingHelper.update_key) && (urlObj.toString().contains("android/version") || urlObj.toString().contains("android/upgrade")))) {
                            Field url = urlObj.getClass().getDeclaredField(urlFieldString);
                            boolean urlAccessible = url.isAccessible();
                            url.setAccessible(true);
                            url.set(urlObj, "https://999.0.0.1/");
                            url.setAccessible(urlAccessible);
                            param.args[0] = request;
                        }
                    }
                }
            });

        Class<?> loadingAdActivityClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.activity.LoadingAdActivity", context.getClassLoader());
        if (loadingAdActivityClass != null)
            findAndHookMethod(loadingAdActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (SettingHelper.getInstance().isEnable(SettingHelper.black_key)) {
                        ((Activity) param.thisObject).finish();
                        param.setResult(null);
                    }
                }
            });
    }
}
