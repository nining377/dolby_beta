package com.raincat.dolby_beta.helper;

import android.content.Context;

import com.raincat.dolby_beta.db.ExtraDao;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/14
 *     desc   : 额外数据帮助类
 *     version: 1.0
 * </pre>
 */

public class ExtraHelper {
    //脚本运行情况，运行中1，未运行0
    public static final String SCRIPT_STATUS = "script_status";
    //APP版本号
    public static final String APP_VERSION = "app_version";

    //用户id
    public static final String USER_ID = "user_id";
    //cookie
    public static final String COOKIE = "cookie";
    //我喜欢的音乐的id
    public static final String LOVE_PLAY_LIST = "play_list";
    //签到时间
    public static final String SIGN_TIME = "sign_time";
    //打卡时间
    public static final String SIGN_SONG_TIME = "sign_song_time";

    //初始化数据库
    public static void init(Context context) {
        ExtraDao.init(context);
    }

    public static String getExtraDate(String key) {
        return ExtraDao.getInstance().getExtra(key);
    }

    public static void setExtraDate(String key, Object value) {
        ExtraDao.getInstance().saveExtra(key, value.toString());
    }

    /**
     * 清除当前用户的数据
     */
    public static void cleanUserData() {
        setExtraDate(COOKIE, "-1");
        setExtraDate(USER_ID, "-1");
        setExtraDate(LOVE_PLAY_LIST, "-1");
        setExtraDate(SIGN_TIME, "-1");
        setExtraDate(SIGN_SONG_TIME, "-1");
    }
}
