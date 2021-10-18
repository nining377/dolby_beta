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
    /**
     * os : pc
     * appver : 2.7.1.198242
     * deviceId : ECFB0F5C9DAF813109DC966057D54B616F0D1E20001A7DDA7111
     * requestId : 62271990
     * clientSign : 60:45:CB:9A:C3:5E@@@WD-WCC2E6LCUS2U@@@@@@39cda0b9-b0aa-4e38-a7d5-e5e9b2f430176d0b275515819c796da324b0129703e2
     * osver : Microsoft-Windows-10-Professional-build-18363-64bit
     * batch-method : POST
     * MUSIC_U : 39f1966f44c8e22a89fbc463eb1372931280b53c277c1386dd3a549e2fd47d9e9e91dee5e2cb62037c15c83028cd465f8bafcdfe5ad2b092
     */

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
