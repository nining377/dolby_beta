package com.raincat.dolby_beta.model;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/03
 *     desc   : 用户信息
 *     version: 1.0
 * </pre>
 */

public class UserInfoBean {
    private int code;
    private ProfileBean profile;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ProfileBean getProfile() {
        if (profile == null)
            profile = new ProfileBean();
        return profile;
    }

    public void setProfile(ProfileBean profile) {
        this.profile = profile;
    }

    public static class ProfileBean {
        private long userId = -1;
        private int userType = 0;
        private String nickname = "";

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public int getUserType() {
            return userType;
        }

        public void setUserType(int userType) {
            this.userType = userType;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
