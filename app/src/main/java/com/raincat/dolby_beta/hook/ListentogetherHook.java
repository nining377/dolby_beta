package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.content.SharedPreferences;


import com.raincat.dolby_beta.helper.ExtraHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;




import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class ListentogetherHook {

    private final SharedPreferences listening;

    public ListentogetherHook(Context context,int versionCode) {
        //旧版写法
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
        //新版写法
        listening = context.getSharedPreferences("LISTEN_TOGETHER", Context.MODE_MULTI_PROCESS);
        XposedBridge.hookAllMethods(findClass("com.netease.cloudmusic.activity.PlayerActivity", context.getClassLoader()), "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                findAndHookMethod(findClass("com.netease.cloudmusic.module.listentogether.meta.RoomInfo", context.getClassLoader()),
                        "getUnlockedIdentity", XC_MethodReplacement.returnConstant(true));
                listening.edit().putBoolean("match_unlock_status" + ExtraHelper.USER_ID, true).apply();
            }
        });

    }
}
