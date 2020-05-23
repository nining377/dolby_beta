package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/23
 *     desc   : 隐藏小红点
 *     version: 1.0
 * </pre>
 */
public class HideBubbleHook {
    public HideBubbleHook(Context context) {
        final Class<?> messageBubbleView = findClass("com.netease.cloudmusic.ui.MessageBubbleView", context.getClassLoader());
        findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (param.thisObject.getClass() == messageBubbleView) {
                    param.args[0] = View.GONE;
                }
            }
        });
    }
}
