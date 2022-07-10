package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.raincat.dolby_beta.db.CloudDao;
import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.EAPIHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;


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
        XposedBridge.hookMethod(ClassHelper.HttpResponse.getResultMethod(context), new XC_MethodHook() {
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
                Object eapi = httpResponse.getEapi(context);
                Uri uri = ClassHelper.HttpUrl.getUri(context, eapi);
                if (!uri.getPath().contains("/eapi/"))
                    return;
                String path = uri.getPath();

                if (path.contains("song/enhance/player/url")) {
                    original = EAPIHelper.modifyPlayer(original);
                } else if (path.contains("song/enhance/download/url")) {
                    JSONObject jsonObject = new JSONObject(original);
                    JSONObject object = jsonObject.getJSONObject("data");
                    JSONArray array = new JSONArray();
                    array.put(object);
                    jsonObject.put("data", array);
                    original = EAPIHelper.modifyPlayer(jsonObject.toString())
                            .replace("[", "").replace("]", "");
                } else if (path.contains("v1/playlist/manipulate/tracks")) {
                    original = EAPIHelper.modifyManipulate(ClassHelper.HttpParams.getParams(context, eapi), original);
                } else if (path.contains("song/like")) {
                    original = EAPIHelper.modifyLike(ClassHelper.HttpParams.getParams(context, eapi), original);
                } else if (path.contains("sound/mobile") || path.contains("page=audio_effect")) {
                    original = EAPIHelper.modifyEffect(original);
                } else if (path.contains("batch")) {
                    if (original.contains("comment\\/banner\\/get")) {
                        JSONObject jsonObject = new JSONObject(original);
                        if (!jsonObject.isNull("/api/content/exposure/comment/banner/get")) {
                            JSONObject object = new JSONObject();
                            object.put("code", 200);
                            object.put("data", new JSONObject());
                            jsonObject.put("/api/content/exposure/comment/banner/get", object);
                        }
                        if (!jsonObject.isNull("/api/v1/content/exposure/comment/banner/get")) {
                            JSONObject object = jsonObject.getJSONObject("/api/v1/content/exposure/comment/banner/get");
                            JSONObject data = object.getJSONObject("data");
                            data.put("count", 0);
                            data.put("offset", 999999999);
                            data.put("records", new JSONArray());
                            data.put("message", "");
                            object.put("data", data);
                            jsonObject.put("/api/v1/content/exposure/comment/banner/get", object);
                        }
                        original = jsonObject.toString();
                    } else if (SettingHelper.getInstance().isEnable(SettingHelper.fix_comment_key) &&
                            original.contains("\\/api\\/resource\\/comment\\/musiciansaid\\/authors")) {
                        JSONObject jsonObject = new JSONObject(original);
                        JSONObject object = jsonObject.getJSONObject("/api/resource/comment/musiciansaid/authors");
                        JSONObject data = object.getJSONObject("data");
                        JSONArray team = data.getJSONArray("team");
                        for (int i = 0; i < team.length(); i++) {
                            JSONObject o = team.getJSONObject(i);
                            String s = o.optString("authorTypeText");
                            if (s != null && s.equals("作者")) {
                                long uid = o.optLong("uid");
                                long artistId = o.optLong("artistId");
                                if (uid > 2147483647) {
                                    JSONObject artistJSONObject = jsonObject.getJSONObject("/api/auth/artist");
                                    JSONObject authJSONObject = artistJSONObject.getJSONObject("auth");
                                    while (uid > 2147483647)
                                        uid = uid / 10;
                                    authJSONObject.put(artistId + "", uid);
                                    artistJSONObject.put("auth", authJSONObject);
                                    jsonObject.put("/api/auth/artist", artistJSONObject);
                                    original = jsonObject.toString();
                                }
                            }
                        }
                    }
                } else if (path.contains("upload/cloud/info/v2")) {
                    JSONObject jsonObject = new JSONObject(original);
                    jsonObject = jsonObject.getJSONObject("privateCloud");
                    jsonObject = jsonObject.getJSONObject("simpleSong");
                    original = original.replace("\"waitTime\":60,", "\"waitTime\":5,");
                    CloudDao.getInstance(context).saveSong(Integer.parseInt(jsonObject.getString("id")), original);
                } else if (path.contains("cloud/pub/v2")) {
                    String songid = EAPIHelper.decrypt(ClassHelper.HttpParams.getParams(context, eapi).get("params")).getString("songid");
                    EAPIHelper.uploadCloud(songid);
                    original = CloudDao.getInstance(context).getSong(Integer.parseInt(songid));
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
