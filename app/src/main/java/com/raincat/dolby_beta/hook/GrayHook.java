package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.util.Log;

import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
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
            if (songPrivilegeClass != null) {
                Method method = null;
                try {
                    method = songPrivilegeClass.getMethod("setDownloadMaxbr", int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        method = songPrivilegeClass.getMethod("setFreeLevel", int.class);
                    } catch (NoSuchMethodException ex) {
                        Log.w("error", ex.getMessage());
                    }
                }
                if (method != null)
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object object = param.thisObject;
                            long id = (long) XposedHelpers.callMethod(object, "getId");
                            if (id == 0)
                                return;

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

                            try {
                                param.args[0] = maxbr;
                                XposedHelpers.callMethod(object, "setSubPriv", 1);
                                XposedHelpers.callMethod(object, "setSharePriv", 1);
                                XposedHelpers.callMethod(object, "setCommentPriv", 1);
                                XposedHelpers.callMethod(object, "setDownMaxLevel", maxbr);
                                XposedHelpers.callMethod(object, "setPlayMaxLevel", maxbr);
                                if (object.getClass().getDeclaredMethod("setPlayMaxbr", int.class) != null)
                                    XposedHelpers.callMethod(object, "setPlayMaxbr", maxbr);
                            } catch (Exception e) {
                                Log.w("error", e.getMessage());
                            }
                        }
                    });
            }
        }
    }
}