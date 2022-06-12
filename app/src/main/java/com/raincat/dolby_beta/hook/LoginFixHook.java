package com.raincat.dolby_beta.hook;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2022/05/03
 *     desc   : 修复登录
 *     version: 1.0
 * </pre>
 */

public class LoginFixHook {
    public LoginFixHook(Context context) {
        Class<?> neteaseMusicUtilsClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.utils.NeteaseMusicUtils", context.getClassLoader());
        if (neteaseMusicUtilsClass != null) {
            XposedHelpers.findAndHookMethod(neteaseMusicUtilsClass, "serialdata", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args[0].equals("/api/login/cellphone")) {
                        if (((String) param.args[1]).contains("\"checkToken\":\"\"")) {
                            Class<?> watchmanClass = XposedHelpers.findClassIfExists("com.netease.mobsecurity.rjsb.watchman", context.getClassLoader());
                            if (watchmanClass == null)
                                watchmanClass = XposedHelpers.findClassIfExists("com.netease.mobsec.rjsb.watchman", context.getClassLoader());
                            if (watchmanClass != null) {
                                XposedHelpers.callStaticMethod(watchmanClass, "init", context, "YD00000558929251");
                                String checkToken = (String) XposedHelpers.callStaticMethod(watchmanClass, "getToken", "30b0cdd23ed1144a0b78de049edc09824", 500, 2);
                                param.args[1] = ((String) param.args[1]).replaceAll("\"checkToken\":\"\"", "\"checkToken\":\"" + checkToken + "\"");
                            }
                        }
                    }
                }
            });
        }
    }
}
