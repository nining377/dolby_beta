package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import com.raincat.dolby_beta.db.CloudDao;
import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.EAPIHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import org.json.JSONObject;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/16
 *     desc   : 网络访问hook
 *     version: 1.0
 * </pre>
 */

public class EAPIHook {
    public EAPIHook(final Context context) {
        XposedBridge.hookMethod(ClassHelper.HttpResponse.getResultMethod(), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //代理和黑胶都未开启
                if (!SettingHelper.getInstance().isEnable(SettingHelper.black_key)
                        && !SettingHelper.getInstance().isEnable(SettingHelper.proxy_master_key))
                    return;
                //返回参数不对
                if ((!(param.getResult() instanceof String) && !(param.getResult() instanceof JSONObject)))
                    return;
                //返回参数为空
                String original = param.getResult().toString();
                if (TextUtils.isEmpty(original)) {
                    return;
                }
                ClassHelper.HttpResponse httpResponse = new ClassHelper.HttpResponse(param.thisObject);
                Object eapi = httpResponse.getEapi();
                Uri uri = ClassHelper.HttpUrl.getUri(eapi);
                if (!uri.getPath().contains("/eapi/"))
                    return;
                String path = uri.getPath();

                if (path.contains("song/enhance/player/url")) {
                    original = EAPIHelper.modifyPlayer(original);
                } else if (path.contains("song/enhance/download/url")) {
                    original = EAPIHelper.modifyPlayer(original.replace("\"data\":", "\"data\":[").replace("},\"code\"", "}],\"code\""));
                    original = original.replace("[", "").replace("]", "");
                } else if (path.contains("v1/playlist/manipulate/tracks")) {
                    original = EAPIHelper.modifyManipulate(ClassHelper.HttpParams.getParams(eapi), original);
                } else if (path.contains("song/like")) {
                    original = EAPIHelper.modifyLike(ClassHelper.HttpParams.getParams(eapi), original);
                } else if (path.contains("sound/mobile") || path.contains("page=audio_effect")) {
                    original = EAPIHelper.modifyEffect(original);
                } else if (path.contains("batch")) {
                    if (original.contains("comment\\/banner\\/get")) {
                        JSONObject jsonObject = new JSONObject(original);
                        jsonObject.put("/api/content/exposure/comment/banner/get", "{\"code\":200}");
                        original = jsonObject.toString();
                        original = original.replace("\"{\\\"code\\\":200}\"", "{\"code\":200}");
                    }
                    if (original.contains("\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get")) {
                        JSONObject jsonObject = new JSONObject(original);
                        jsonObject.put("/api/v1/content/exposure/comment/banner/get", "{-\"code-\":200,-\"data-\":{-\"count-\":0,-\"offset-\":999999999,-\"records-\":[]},-\"message-\":-\"-\"}");
                        original = jsonObject.toString();
                        original = original.replace("-\\", "").replace("\"\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get\":\"", "\"\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get\":")
                                .replace("\"message\":\"\"}\"", "\"message\":\"\"}");
                    }
                    original = EAPIHelper.modifyByRegex(original);
                } else if (path.contains("upload/cloud/info/v2")) {
                    JSONObject jsonObject = new JSONObject(original);
                    jsonObject = jsonObject.getJSONObject("privateCloud");
                    jsonObject = jsonObject.getJSONObject("simpleSong");
                    original = original.replace("\"waitTime\":60,", "\"waitTime\":5,");
                    CloudDao.getInstance(context).saveSong(Integer.parseInt(jsonObject.getString("id")), original);
                } else if (path.contains("cloud/pub/v2")) {
                    String songid = EAPIHelper.decrypt(ClassHelper.HttpParams.getParams(eapi).get("params")).getString("songid");
                    EAPIHelper.uploadCloud(songid);
                    original = CloudDao.getInstance(context).getSong(Integer.parseInt(songid));
                } else if (path.contains("album") || path.contains("artist") || path.contains("play")
                        || path.contains("radio") || path.contains("song") || path.contains("search")) {
                    original = EAPIHelper.modifyByRegex(original);
                }

                param.setResult(param.getResult() instanceof JSONObject ? new JSONObject(original) : original);
            }
        });
    }

    private void logcat(String msg) {
        int max_str_length = 1800;
        //大于4000时
        while (msg.length() > max_str_length) {
            XposedBridge.log(msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        XposedBridge.log(msg);
    }
}
