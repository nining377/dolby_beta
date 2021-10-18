package com.raincat.dolby_beta.helper;

import com.google.gson.Gson;
import com.raincat.dolby_beta.model.UserInfoBean;
import com.raincat.dolby_beta.net.Http;

import java.util.HashMap;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/15
 *     desc   : 用户状态帮助类
 *     version: 1.0
 * </pre>
 */

public class UserHelper {
    /**
     * 通过cookie获取用户信息
     */
    public static void getUserInfo() {
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("cookie", ExtraHelper.getExtraDate(ExtraHelper.COOKIE));
        String userInfo = new Http("GET", "https://music.163.com/api/nuser/account/get", headers, (String) null).getResult();
        Gson gson = new Gson();
        UserInfoBean userInfoBean = gson.fromJson(userInfo, UserInfoBean.class);
        ExtraHelper.setExtraDate(ExtraHelper.USER_ID, userInfoBean.getProfile().getUserId());
    }
}
