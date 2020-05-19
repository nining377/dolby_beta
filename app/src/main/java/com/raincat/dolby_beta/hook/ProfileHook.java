package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.utils.Setting;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callMethod;

/**
 * <pre>
 *     author : RainCat
 *
 *     time   : 2020/04/14
 *     desc   : 个人资料hook
 *     version: 1.0
 * </pre>
 */
public class ProfileHook {
    private static long userId = -1;
    private static int level = -1;
    private static int follows = -1;
    private static int fans = -1;
    private static int role = -1;

    public ProfileHook(Context context) {
        level = Setting.getLevel();
        follows = Setting.getFollows();
        fans = Setting.getFans();
        role = Setting.getRole();

        if (level != -1)
            XposedHelpers.findAndHookMethod("com.netease.cloudmusic.meta.Profile", context.getClassLoader(), "setLevel", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (userId == -1) {
                                String userIdString = ExtraDao.getInstance(context).getExtra("userId");
                                if (userIdString.length() != 0)
                                    userId = Long.parseLong(userIdString);
                                else
                                    return;
                            }
                            Object object = param.thisObject;
                            if (userId == (long) callMethod(object, "getUserId"))
                                param.args[0] = level;
                        }
                    });
        if (follows != -1)
            XposedHelpers.findAndHookMethod("com.netease.cloudmusic.meta.Profile", context.getClassLoader(), "getFollows",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (userId == -1) {
                                String userIdString = ExtraDao.getInstance(context).getExtra("userId");
                                if (userIdString.length() != 0)
                                    userId = Long.parseLong(userIdString);
                                else
                                    return;
                            }
                            Object object = param.thisObject;
                            if (userId == (long) callMethod(object, "getUserId"))
                                param.setResult(follows);
                        }
                    });

        if (fans != -1)
            XposedHelpers.findAndHookMethod("com.netease.cloudmusic.meta.Profile", context.getClassLoader(), "getFolloweds",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (userId == -1) {
                                String userIdString = ExtraDao.getInstance(context).getExtra("userId");
                                if (userIdString.length() != 0)
                                    userId = Long.parseLong(userIdString);
                                else
                                    return;
                            }
                            Object object = param.thisObject;
                            if (userId == (long) callMethod(object, "getUserId"))
                                param.setResult(fans);
                        }
                    });

        if (role != -1)
            XposedHelpers.findAndHookMethod("com.netease.cloudmusic.meta.Profile", context.getClassLoader(), "setUserType", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (userId == -1) {
                                String userIdString = ExtraDao.getInstance(context).getExtra("userId");
                                if (userIdString.length() != 0)
                                    userId = Long.parseLong(userIdString);
                                else
                                    return;
                            }
                            Object object = param.thisObject;
                            if (userId == (long) callMethod(object, "getUserId"))
                                param.args[0] = role;
                        }
                    });
    }
}
