package com.raincat.dolby_beta.model;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/27
 *     desc   : 用户会员信息
 *     version: 1.0
 * </pre>
 */

public class UserPrivilegeBean {
    private int code;
    private DataBean data;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        private AssociatorBean associator;
        private MusicPackageBean musicPackage;
        private long now;
        private boolean oldCacheProtocol;
        private int redVipAnnualCount;
        private int redVipLevel;
        private long userId;

        public AssociatorBean getAssociator() {
            if (associator == null)
                associator = new AssociatorBean();
            return associator;
        }

        public void setAssociator(AssociatorBean associator) {
            this.associator = associator;
        }

        public MusicPackageBean getMusicPackage() {
            if (musicPackage == null)
                musicPackage = new MusicPackageBean();
            return musicPackage;
        }

        public void setMusicPackage(MusicPackageBean musicPackage) {
            this.musicPackage = musicPackage;
        }

        public long getNow() {
            return now;
        }

        public void setNow(long now) {
            this.now = now;
        }

        public boolean isOldCacheProtocol() {
            return oldCacheProtocol;
        }

        public void setOldCacheProtocol(boolean oldCacheProtocol) {
            this.oldCacheProtocol = oldCacheProtocol;
        }

        public int getRedVipAnnualCount() {
            return redVipAnnualCount;
        }

        public void setRedVipAnnualCount(int redVipAnnualCount) {
            this.redVipAnnualCount = redVipAnnualCount;
        }

        public int getRedVipLevel() {
            return redVipLevel;
        }

        public void setRedVipLevel(int redVipLevel) {
            this.redVipLevel = redVipLevel;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public static class AssociatorBean {
            private long expireTime = 0;
            private boolean isSign = false;
            private boolean isSignDeduct = false;
            private boolean isSignIap = false;
            private boolean isSignIapDeduct = false;
            private int vipCode = 100;

            public long getExpireTime() {
                return expireTime;
            }

            public void setExpireTime(long expireTime) {
                this.expireTime = expireTime;
            }

            public boolean isIsSign() {
                return isSign;
            }

            public void setIsSign(boolean isSign) {
                this.isSign = isSign;
            }

            public boolean isIsSignDeduct() {
                return isSignDeduct;
            }

            public void setIsSignDeduct(boolean isSignDeduct) {
                this.isSignDeduct = isSignDeduct;
            }

            public boolean isIsSignIap() {
                return isSignIap;
            }

            public void setIsSignIap(boolean isSignIap) {
                this.isSignIap = isSignIap;
            }

            public boolean isIsSignIapDeduct() {
                return isSignIapDeduct;
            }

            public void setIsSignIapDeduct(boolean isSignIapDeduct) {
                this.isSignIapDeduct = isSignIapDeduct;
            }

            public int getVipCode() {
                return vipCode;
            }

            public void setVipCode(int vipCode) {
                this.vipCode = vipCode;
            }
        }

        public static class MusicPackageBean {
            private long expireTime = 0;
            private boolean isSign = false;
            private boolean isSignDeduct = false;
            private boolean isSignIap = false;
            private boolean isSignIapDeduct = false;
            private int vipCode = 220;

            public long getExpireTime() {
                return expireTime;
            }

            public void setExpireTime(long expireTime) {
                this.expireTime = expireTime;
            }

            public boolean isIsSign() {
                return isSign;
            }

            public void setIsSign(boolean isSign) {
                this.isSign = isSign;
            }

            public boolean isIsSignDeduct() {
                return isSignDeduct;
            }

            public void setIsSignDeduct(boolean isSignDeduct) {
                this.isSignDeduct = isSignDeduct;
            }

            public boolean isIsSignIap() {
                return isSignIap;
            }

            public void setIsSignIap(boolean isSignIap) {
                this.isSignIap = isSignIap;
            }

            public boolean isIsSignIapDeduct() {
                return isSignIapDeduct;
            }

            public void setIsSignIapDeduct(boolean isSignIapDeduct) {
                this.isSignIapDeduct = isSignIapDeduct;
            }

            public int getVipCode() {
                return vipCode;
            }

            public void setVipCode(int vipCode) {
                this.vipCode = vipCode;
            }
        }
    }
}
