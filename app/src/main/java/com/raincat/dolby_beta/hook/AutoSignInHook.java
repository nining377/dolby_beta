package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.widget.TextView;

import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.helper.SignSongHelper;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.Tools;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/09/26
 *     desc   : 自动签到hook
 *     version: 1.0
 * </pre>
 */

public class AutoSignInHook {
    private String methodInitDrawerHeader = "initDrawerHeader";
    private String valueDrawerUserSignIn = "drawerUserSignIn";

    public AutoSignInHook(Context context, int versionCode) {
        //每天0点签到
        findAndHookMethod("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader(),
                "onStart", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (SettingHelper.getInstance().isEnable(SettingHelper.sign_key) || SettingHelper.getInstance().isEnable(SettingHelper.sign_song_key)) {
                            String userId = ExtraHelper.getExtraDate(ExtraHelper.USER_ID);
                            String cookie = ExtraHelper.getExtraDate(ExtraHelper.COOKIE);
                            if (userId.equals("-1") || cookie.equals("-1"))
                                return;
                            if (SettingHelper.getInstance().isEnable(SettingHelper.sign_key)) {
                                long lastSignInTime = Long.parseLong(ExtraHelper.getExtraDate(ExtraHelper.SIGN_TIME + userId));
                                if (lastSignInTime < Tools.getTodayStartTime()) {
                                    sign(context, cookie);
                                    ExtraHelper.setExtraDate(ExtraHelper.SIGN_TIME + userId, System.currentTimeMillis());
                                }
                            }
                            if (SettingHelper.getInstance().isEnable(SettingHelper.sign_song_key)) {
                                long lastSignInTime = Long.parseLong(ExtraHelper.getExtraDate(ExtraHelper.SIGN_SONG_TIME + userId));
                                if (lastSignInTime < Tools.getTodayStartTime()) {
                                    SignSongHelper.showSignStatusDialog((Context) param.thisObject, SettingHelper.sign_song_title, null);
                                    ExtraHelper.setExtraDate(ExtraHelper.SIGN_SONG_TIME + userId, System.currentTimeMillis());
                                }
                            }
                        }
                    }
                });

        //更改当前签到状态
        Class<?> userProfileClass = findClassIfExists("com.netease.cloudmusic.meta.Profile", context.getClassLoader());
        if (userProfileClass != null) {
            findAndHookMethod(userProfileClass, "isMobileSign", XC_MethodReplacement.returnConstant(true));
        }

        Class<?> mainDrawerClass = findClassIfExists("com.netease.cloudmusic.ui.MainDrawer", context.getClassLoader());
        if (mainDrawerClass == null) {
            mainDrawerClass = findClassIfExists("com.netease.cloudmusic.ui.l", context.getClassLoader());
            methodInitDrawerHeader = "r";
            valueDrawerUserSignIn = "t";
        }

        //更改当前签到状态文字
        if (versionCode < 7003000 && mainDrawerClass != null) {
            findAndHookMethod(mainDrawerClass, methodInitDrawerHeader, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Field drawerUserSignInField = param.thisObject.getClass().getDeclaredField(valueDrawerUserSignIn);
                    drawerUserSignInField.setAccessible(true);
                    TextView drawerUserSignIn = (TextView) drawerUserSignInField.get(param.thisObject);
                    drawerUserSignIn.setText("已签到");
                    drawerUserSignIn.setEnabled(false);
                    drawerUserSignIn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            });
        }
    }

    /**
     * 签到
     */
    private void sign(Context context, String cookie) {
        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", cookie);

        HashMap<String, Object> param = new HashMap<>();
        param.put("type", "1");
        new Http("POST", "http://music.163.com/api/point/dailyTask", param, header).getResult();

        param.put("type", "0");
        String result = new Http("POST", "http://music.163.com/api/point/dailyTask", param, header).getResult();
        if (result.contains("200") && !result.contains("msg"))
            Tools.showToastOnLooper(context, "自动签到成功");
    }
}
