package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.SettingHelper;

import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 不变灰
 *     version: 1.0
 * </pre>
 */

public class GrayHook {
    public GrayHook(Context context) {
        if (SettingHelper.getInstance().isEnable(SettingHelper.proxy_gray_key))
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.MusicInfo", context.getClassLoader()),
                    "hasCopyRight", XC_MethodReplacement.returnConstant(true));
    }
}