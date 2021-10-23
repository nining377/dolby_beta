package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;

import com.raincat.dolby_beta.helper.SettingHelper;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

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
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_bubble_hide_key))
            return;
        final Class<?> messageBubbleView = findClassIfExists("com.netease.cloudmusic.ui.MessageBubbleView", context.getClassLoader());
        final Class<?> messageBubbleView_800 = findClassIfExists("com.netease.cloudmusic.theme.ui.MessageBubbleView", context.getClassLoader());
        findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if ((messageBubbleView != null && param.thisObject.getClass() == messageBubbleView) || (messageBubbleView_800 != null && param.thisObject.getClass() == messageBubbleView_800)) {
                    param.args[0] = View.GONE;
                }
            }
        });
    }
}