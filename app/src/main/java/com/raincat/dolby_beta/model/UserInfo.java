package com.raincat.dolby_beta.model;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/24
 *     desc   : 网易云返回的用户信息
 *     version: 1.0
 * </pre>
 */

public class UserInfo {
    private int level = 0;
    private UserPointBean userPoint;
    private boolean mobileSign = true;
    private boolean pcSign = true;
    private int viptype = 0;
    private long expiretime = 0L;
    private long backupExpireTime = 0L;
    private String storeurl = "";
    private String mallDesc = "";
    private String storeTitle = "";
    private String pubwords = "";
    private Object gameConfig;
    private Object ringConfig;
    private Object fmConfig;
    private TicketConfigBean ticketConfig;
    private int code = 0;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public UserPointBean getUserPoint() {
        if (userPoint == null)
            userPoint = new UserPointBean();
        return userPoint;
    }

    public void setUserPoint(UserPointBean userPoint) {
        this.userPoint = userPoint;
    }

    public boolean isMobileSign() {
        return mobileSign;
    }

    public void setMobileSign(boolean mobileSign) {
        this.mobileSign = mobileSign;
    }

    public boolean isPcSign() {
        return pcSign;
    }

    public void setPcSign(boolean pcSign) {
        this.pcSign = pcSign;
    }

    public int getViptype() {
        return viptype;
    }

    public void setViptype(int viptype) {
        this.viptype = viptype;
    }

    public long getExpiretime() {
        return expiretime;
    }

    public void setExpiretime(long expiretime) {
        this.expiretime = expiretime;
    }

    public long getBackupExpireTime() {
        return backupExpireTime;
    }

    public void setBackupExpireTime(long backupExpireTime) {
        this.backupExpireTime = backupExpireTime;
    }

    public String getStoreurl() {
        return storeurl;
    }

    public void setStoreurl(String storeurl) {
        this.storeurl = storeurl;
    }

    public String getMallDesc() {
        return mallDesc;
    }

    public void setMallDesc(String mallDesc) {
        this.mallDesc = mallDesc;
    }

    public String getStoreTitle() {
        return storeTitle;
    }

    public void setStoreTitle(String storeTitle) {
        this.storeTitle = storeTitle;
    }

    public String getPubwords() {
        return pubwords;
    }

    public void setPubwords(String pubwords) {
        this.pubwords = pubwords;
    }

    public Object getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Object gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Object getRingConfig() {
        return ringConfig;
    }

    public void setRingConfig(Object ringConfig) {
        this.ringConfig = ringConfig;
    }

    public Object getFmConfig() {
        return fmConfig;
    }

    public void setFmConfig(Object fmConfig) {
        this.fmConfig = fmConfig;
    }

    public TicketConfigBean getTicketConfig() {
        return ticketConfig;
    }

    public void setTicketConfig(TicketConfigBean ticketConfig) {
        this.ticketConfig = ticketConfig;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class UserPointBean {
        private int balance = 0;
        private int blockBalance = 0;
        private int status = 0;
        private long updateTime = 0L;
        private int userId = 0;
        private int version = 0;

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public int getBlockBalance() {
            return blockBalance;
        }

        public void setBlockBalance(int blockBalance) {
            this.blockBalance = blockBalance;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }

    public static class TicketConfigBean {
    }
}
