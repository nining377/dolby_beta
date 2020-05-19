package com.raincat.dolby_beta.hook;

import android.content.Context;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/26
 *     desc   : 黑胶
 *     version: 1.0
 * </pre>
 */

public class BlackHook {
    public BlackHook(Context context, int versionCode) {
        if (versionCode < 138) {
            //黑胶
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.Profile", context.getClassLoader()),
                    "isVipPro", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.Profile", context.getClassLoader()),
                    "getVipProExpireTime", XC_MethodReplacement.returnConstant(System.currentTimeMillis() + 31536000000L));

            //音乐包
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.Profile", context.getClassLoader()),
                    "isVip", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.Profile", context.getClassLoader()),
                    "getExpireTime", XC_MethodReplacement.returnConstant(System.currentTimeMillis() + 31536000000L));

            //主题
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "i", XC_MethodReplacement.returnConstant(0));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "j", XC_MethodReplacement.returnConstant("免费"));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "o", XC_MethodReplacement.returnConstant(false));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "s", XC_MethodReplacement.returnConstant(false));
        } else {
            //黑胶
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.UserPrivilege", context.getClassLoader()),
                    "isBlackVip", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.UserPrivilege", context.getClassLoader()),
                    "getBlackVipExpireTime", XC_MethodReplacement.returnConstant(System.currentTimeMillis() + 31536000000L));

            //音乐包
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.UserPrivilege", context.getClassLoader()),
                    "isWhateverMusicPackage", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.UserPrivilege", context.getClassLoader()),
                    "getMusicPackageExpireTime", XC_MethodReplacement.returnConstant(System.currentTimeMillis() + 31536000000L));

            //主题
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "getPoints", XC_MethodReplacement.returnConstant(0));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "getPrice", XC_MethodReplacement.returnConstant("免费"));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "isVip", XC_MethodReplacement.returnConstant(false));
            findAndHookMethod(findClass("com.netease.cloudmusic.theme.core.ThemeInfo", context.getClassLoader()),
                    "isDigitalAlbum", XC_MethodReplacement.returnConstant(false));
        }

        //音质切换
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "isVipFee", XC_MethodReplacement.returnConstant(false));
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getPlayMaxLevel", XC_MethodReplacement.returnConstant(999000));
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getFee", XC_MethodReplacement.returnConstant(0));
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getPayed", XC_MethodReplacement.returnConstant(0));
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "getFlag", XC_MethodReplacement.returnConstant(0));
        XposedBridge.hookAllMethods(findClass("com.netease.cloudmusic.meta.virtual.ResourcePrivilege", context.getClassLoader()),
                "isFee", XC_MethodReplacement.returnConstant(false));
        findAndHookMethod(findClass("com.netease.cloudmusic.meta.virtual.SongPrivilege", context.getClassLoader()),
                "canShare", XC_MethodReplacement.returnConstant(true));
    }
}
