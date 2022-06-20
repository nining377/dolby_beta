package com.raincat.dolby_beta.helper;

import com.google.gson.Gson;
import com.ndktools.javamd5.core.MD5;
import com.raincat.dolby_beta.model.CloudHeader;
import com.raincat.dolby_beta.model.NeteaseSongListBean;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES2;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/16
 *     desc   : 接口处理
 *     version: 1.0
 * </pre>
 */

public class EAPIHelper {
    private static final Gson gson = new Gson();

    /**
     * 解除下载加密
     */
    public static String modifyPlayer(String original) {
        NeteaseSongListBean listBean = gson.fromJson(original, NeteaseSongListBean.class);

        NeteaseSongListBean modifyListBean = new NeteaseSongListBean();
        modifyListBean.setCode(200);
        modifyListBean.setData(new ArrayList<>());
        for (NeteaseSongListBean.DataBean dataBean : listBean.getData()) {
            //flag与8非0为云盘歌曲
            if ((dataBean.getFlag() & 0x8) == 0) {

                dataBean.setFee(0);
                dataBean.setFlag(0);
                dataBean.setPayed(0);
                dataBean.setFreeTrialInfo(null);
                if (dataBean.getUrl() != null && dataBean.getUrl().contains("?"))
                    dataBean.setUrl(dataBean.getUrl().substring(0, dataBean.getUrl().indexOf("?")));
            }
            modifyListBean.getData().add(dataBean);
        }
        return gson.toJson(modifyListBean);
    }

    /**
     * 收藏
     */
    public static String modifyManipulate(HashMap<String, String> data, String original) throws Exception {
        if (original.contains("\"code\":200") && original.contains("\"offlineIds\":[]") && !original.contains("\"trackIds\":\"[]\""))
            return original;

        String cookie = ExtraHelper.getExtraDate(ExtraHelper.COOKIE);
        if (cookie.equals("-1")) {
            return original;
        }
        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", cookie);

        JSONObject paramJSON = decrypt(data.get("params"));
        HashMap<String, Object> param = new HashMap<>();
        String trackIds = paramJSON.getString("trackIds");
        param.put("op", paramJSON.getString("op"));
        param.put("pid", paramJSON.getString("pid"));

        String newTrackIds = trackIds.replace("]", "") + trackIds.replace("[", ",");
        param.put("trackIds", newTrackIds);
        String result = new Http("POST", "http://music.163.com/api/playlist/manipulate/tracks", param, header).getResult();
        if (result.contains("502") || result.contains("200"))
            result = "{\"trackIds\":" + trackIds + ",\"code\":200,\"privateCloudStored\":false}";
        return result;
    }

    /**
     * 喜欢
     */
    public static String modifyLike(HashMap<String, String> data, String original) throws Exception {
        String cookie = ExtraHelper.getExtraDate(ExtraHelper.COOKIE);
        String pid = ExtraHelper.getExtraDate(ExtraHelper.LOVE_PLAY_LIST);
        if (original.contains("\"code\":200") || cookie.equals("-1") || pid.equals("-1"))
            return original;

        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", cookie);

        //获取我喜欢的音乐列表
        JSONObject paramJSON = decrypt(data.get("params"));
        String trackId = paramJSON.getString("trackId");

        HashMap<String, Object> param = new HashMap<>();
        param.put("trackIds", "[\"" + trackId + "\",\"" + trackId + "\"]");
        param.put("op", "add");
        param.put("pid", pid);

        String result = new Http("POST", "http://music.163.com/api/playlist/manipulate/tracks", param, header).getResult();
        if (result.contains("502") || result.contains("200"))
            result = "{\"playlistId\":" + pid + ",\"code\":200}";
        return result;
    }

    public static void uploadCloud(String data) {
        String paramString = "{\"songid\":\"" + data + "\",\"e_r\":true,\"header\":\"%s\"}";
        CloudHeader cloudHeader = new CloudHeader();
        cloudHeader.setOs("pc");
        cloudHeader.setAppver("2.7.1.198242");
//        cloudHeader.setDeviceId(ExtraDao.getInstance(context).getExtra("deviceId"));
        Random random = new Random();
        cloudHeader.setRequestId(String.valueOf(random.nextInt() * (1000000 - 10000 + 1) + 10000));
        cloudHeader.setClientSign("60:45:CB:9A:C3:5E@@@WD-WCC2E6LCUS2U@@@@@@39cda0b9-b0aa-4e38-a7d5-e5e9b2f430176d0b275515819c796da324b0129703e2");
        cloudHeader.setOsver("Microsoft-Windows-10-Professional-build-18363-64bit");
        cloudHeader.setBatchmethod("POST");
        cloudHeader.setMUSIC_U(ExtraHelper.getExtraDate(ExtraHelper.COOKIE).replace("MUSIC_U=", ""));

        Gson gson = new Gson();
        String headerParam = gson.toJson(cloudHeader);
        headerParam = headerParam.replace("\"", "\\\"");
        paramString = String.format(paramString, headerParam);
        MD5 md5 = new MD5();
        String md5String = md5.getMD5ofStr("nobody" + "/api/cloud/pub/v2" + "use" + paramString + "md5forencrypt");
        paramString = "/api/cloud/pub/v2-36cd479b6b5-" + paramString + "-36cd479b6b5-" + md5String.toLowerCase();

        HashMap<String, Object> header = new HashMap<>();
        header.put("Host", "interface3.music.163.com");
        header.put("Connection", "keep-alive");
        header.put("Accept", "*/*");
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Origin", "orpheus://orpheus");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.157 NeteaseMusicDesktop/2.7.1.198242 Safari/537.36");
        header.put("Accept-Encoding", "gzip,deflate");
        header.put("Accept-Language", "en-us,en;q=0.8");

        paramString = NeteaseAES2.Encrypt(paramString);
        HashMap<String, Object> param = new HashMap<>();
        param.put("params", paramString);

        new Http("POST", "http://interface3.music.163.com/eapi/cloud/pub/v2", param, header).getResult();
    }

    /**
     * 音效
     */
    public static String modifyEffect(String originalContent) {
        originalContent = Pattern.compile("\"type\":\\d+").matcher(originalContent).replaceAll("\"type\":1");
        return originalContent;
    }

    public static JSONObject decrypt(String params) throws Exception {
        params = NeteaseAES2.Decrypt(params);
        if (params != null && params.length() != 0) {
            params = params.substring(params.indexOf("{"), params.lastIndexOf("}") + 1);
            JSONObject jsonObject = new JSONObject(params);
            if (jsonObject.isNull("params"))
                return new JSONObject(params);
            else
                return decrypt(jsonObject.getString("params"));
        } else
            return new JSONObject();
    }
}
