package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.utils.CloudMusicPackage;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;

import org.json.JSONObject;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/24
 *     desc   : API hook
 *     version: 1.0
 * </pre>
 */

public class EAPIHook extends EAPIBase {
    public EAPIHook(final Context context) {
        XposedBridge.hookMethod(CloudMusicPackage.HttpResponse.getResultMethod(), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((!(param.getResult() instanceof String) && !(param.getResult() instanceof JSONObject))) {
                    return;
                }
                String original = param.getResult().toString();
                if (TextUtils.isEmpty(original)) {
                    return;
                }

                CloudMusicPackage.HttpResponse httpResponse = new CloudMusicPackage.HttpResponse(param.thisObject);
                Object eapi = httpResponse.getEapi();
                Uri uri = CloudMusicPackage.HttpApi.getUri(eapi);
                if (!uri.getPath().contains("/eapi/"))
                    return;

                String path = uri.getPath().substring("/eapi/".length());
                String modified = null;

                if ("v1/playlist/manipulate/tracks".equals(path)) {
                    modified = modifyPlaylistManipulateApi(context, CloudMusicPackage.HttpParams.getParams(eapi), original);
                } else if ("song/like".equals(path)) {
                    modified = modifyLike(context, CloudMusicPackage.HttpParams.getParams(eapi), original);
                } else if (path.contains("sound/mobile") || path.contains("page=audio_effect")) {
                    modified = modifyEffect(original);
                } else if (path.contains("batch")) {
                    getUserId(context, original);
                    //去除评论广告
                    if (original.contains("comment\\/banner\\/get")) {
                        JSONObject jsonObject = new JSONObject(original);
                        jsonObject.put("/api/content/exposure/comment/banner/get", "{\"code\":200}");
                        modified = jsonObject.toString();
                        modified = modified.replace("\"{\\\"code\\\":200}\"", "{\"code\":200}");
                    }
                    if (original.contains("\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get")) {
                        JSONObject jsonObject = new JSONObject(original);
                        jsonObject.put("/api/v1/content/exposure/comment/banner/get", "{-\"code-\":200,-\"data-\":{-\"count-\":0,-\"offset-\":999999999,-\"records-\":[]},-\"message-\":-\"-\"}");
                        modified = jsonObject.toString();
                        modified = modified.replace("-\\", "").replace("\"\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get\":\"", "\"\\/api\\/v1\\/content\\/exposure\\/comment\\/banner\\/get\":")
                                .replace("\"message\":\"\"}\"", "\"message\":\"\"}");
                    }
                    //解除灰色
                    if (Setting.isGrayEnabled())
                        modified = modifyByRegex(modified == null ? original : modified);
                } else if (path.contains("login") || path.contains("captcha")) {
                    Object response = httpResponse.getResponseObject();
                    CloudMusicPackage.OKHttp3Response okHttp3Response = new CloudMusicPackage.OKHttp3Response(response);
                    Object header = okHttp3Response.getHeadersObject();
                    CloudMusicPackage.OKHttp3Header okHttp3Header = new CloudMusicPackage.OKHttp3Header(header);
                    String[] headers = okHttp3Header.getHeaders();
                    Start:
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].toLowerCase().contains("set-cookie")) {
                            String[] cookies = headers[i + 1].split(";");
                            for (String cookie : cookies) {
                                if (cookie.contains("MUSIC_U")) {
                                    ExtraDao.getInstance(context).saveExtra("cookie", cookie);
                                    break Start;
                                }
                            }
                        }
                    }
                } else {
                    if (!Setting.isGrayEnabled())
                        return;
                    List<String> segments = uri.getPathSegments();
                    if (segments.contains("album")
                            || segments.contains("artist")
                            || segments.contains("play")
                            || segments.contains("playlist")
                            || segments.contains("radio")
                            || segments.contains("song")
                            || segments.contains("songs")
                            || segments.contains("search")) {
                        modified = modifyByRegex(original);
                    }
                }

                if (modified != null) {
                    param.setResult(param.getResult() instanceof JSONObject ? new JSONObject(modified) : modified);
                }
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
