package com.raincat.dolby_beta.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodReplacement;


import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class ListentogetherHook {
    public ListentogetherHook(Context context, int versionCode) {
        if (versionCode > 8007090) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.f2", context.getClassLoader()),
                    "v", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8007075) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "v", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8007070) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.y", context.getClassLoader()),
                    "u", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8007055) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "u", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8007026) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.w", context.getClassLoader()),
                    "o", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8007004) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.w", context.getClassLoader()),
                    "n", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8006076) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.u", context.getClassLoader()),
                    "m", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8006045) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.r", context.getClassLoader()),
                    "l1", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8006040) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.p", context.getClassLoader()),
                    "h1", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode > 8006019) {
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "n1", XC_MethodReplacement.returnConstant(true));
        }else if (versionCode >= 8006000){
            findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.x", context.getClassLoader()),
                    "m1", XC_MethodReplacement.returnConstant(true));
        }
    }
}
