package com.raincat.dolby_beta.model;

import com.google.gson.annotations.SerializedName;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/03/24
 *     desc   : 上传云盘的请求头
 *     version: 1.0
 * </pre>
 */
public class CloudHeader {
    private String os;
    private String appver;
    private String deviceId;
    private String requestId;
    private String clientSign;
    private String osver;
    @SerializedName("batch-method")
    private String batchmethod;
    private String MUSIC_U;

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getAppver() {
        return appver;
    }

    public void setAppver(String appver) {
        this.appver = appver;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientSign() {
        return clientSign;
    }

    public void setClientSign(String clientSign) {
        this.clientSign = clientSign;
    }

    public String getOsver() {
        return osver;
    }

    public void setOsver(String osver) {
        this.osver = osver;
    }

    public String getBatchmethod() {
        return batchmethod;
    }

    public void setBatchmethod(String batchmethod) {
        this.batchmethod = batchmethod;
    }

    public String getMUSIC_U() {
        return MUSIC_U;
    }

    public void setMUSIC_U(String MUSIC_U) {
        this.MUSIC_U = MUSIC_U;
    }
}
