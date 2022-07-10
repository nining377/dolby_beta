package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.ClassHelper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/13
 *     desc   : 绕过CDN责任链拦截器检测
 *     version: 1.0
 * </pre>
 */

public class CdnHook {
    public CdnHook(Context context, int versionCode) {
        if (versionCode < 138)
            return;
        for (Method m : ClassHelper.HttpInterceptor.getMethodList(context))
            XposedBridge.hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.setResult(param.args[2]);
                }
            });
    }
}
