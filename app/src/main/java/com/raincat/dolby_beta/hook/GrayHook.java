package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

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

        if (SettingHelper.getInstance().isEnable(SettingHelper.proxy_master_key)) {
            Class<?> songPrivilegeClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.meta.virtual.SongPrivilege", context.getClassLoader());
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.netease.cloudmusic.meta.MusicInfo", context.getClassLoader()), "setSp", songPrivilegeClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object object = param.args[0];
                    Field[] fields = object.getClass().getDeclaredFields();
                    int maxbr = 0;
                    for (Field field : fields) {
                        if (field.getType() == int.class && field.getName().equals("maxbr")) {
                            field.setAccessible(true);
                            maxbr = (int) field.get(object);
                            break;
                        }
                    }
                    if (maxbr == 0)
                        maxbr = 999000;

                    XposedHelpers.callMethod(object, "setFreeLevel", maxbr);
                    XposedHelpers.callMethod(object, "setSubPriv", 1);
                    XposedHelpers.callMethod(object, "setDownMaxLevel", maxbr);
                    XposedHelpers.callMethod(object, "setPlayMaxLevel", maxbr);
                    XposedHelpers.callMethod(object, "setDownloadMaxbr", maxbr);
                    XposedHelpers.callMethod(object, "setPlayMaxbr", maxbr);
                }
            });
        }
    }
}