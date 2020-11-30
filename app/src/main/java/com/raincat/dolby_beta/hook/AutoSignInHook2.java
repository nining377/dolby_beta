package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.db.SignDao;
import com.raincat.dolby_beta.model.DailyRecommend;
import com.raincat.dolby_beta.model.PlaylistDetail;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * <pre>
 *     author : RainCat
 *     org    : Shenzhen JingYu Network Technology Co., Ltd.
 *     e-mail : nining377@gmail.com
 *     time   : 2020/09/26
 *     desc   : 自动签到hook
 *     version: 1.0
 * </pre>
 */

public class AutoSignInHook2 {
    public AutoSignInHook2(Context context) {
        //每天0点签到
        findAndHookMethod("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader(),
                "onStart", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("com.netease.cloudmusic.preferences", Context.MODE_MULTI_PROCESS);
                        String userId = ExtraDao.getInstance(context).getExtra("userId");
                        long lastSignInTime = sharedPreferences.getLong("lastSignInTime_" + userId, 0L);
                        if (lastSignInTime < getTodayStartTime()) {
                            sign(context);
                            signSong(context);
                            sharedPreferences.edit().putLong("lastSignInTime_" + userId, System.currentTimeMillis()).apply();
                        }
                    }
                });
    }

    /**
     * 签到
     */
    private void sign(Context context) {
        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", ExtraDao.getInstance(context).getExtra("cookie"));

        HashMap<String, Object> param = new HashMap<>();
        param.put("type", "1");
        new Http("POST", "http://music.163.com/api/point/dailyTask", param, header).getResult();

        param.put("type", "0");
        String result = new Http("POST", "http://music.163.com/api/point/dailyTask", param, header).getResult();
        if (result.contains("200") && !result.contains("msg"))
            Tools.showToastOnLooper(context, "自动签到成功");
    }

    /**
     * 歌曲打卡
     */
    private void signSong(Context context) {
        if (!Setting.isSignSongEnabled())
            return;

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String cookie = ExtraDao.getInstance(context).getExtra("cookie");
            if (cookie.equals("-1")) {
                Tools.showToastOnLooper(context, "打卡失败，请重新登录以获取cookie");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8:00"));
            String userId = ExtraDao.getInstance(context).getExtra("userId");
            String signSongDate = sdf.format(new Date(System.currentTimeMillis()));
            if (ExtraDao.getInstance(context).getExtra("signSongDate" + userId).equals(signSongDate))
                return;

            Tools.showToastOnLooper(context, "每日打卡开始，请尽量保持网易云音乐位于前台");
            Tools.copyFile(Tools.sdcardDbPath, Tools.neteaseDbPath);
            Tools.copyFile(Tools.sdcardDbPath + "-journal", Tools.neteaseDbPath + "-journal");

            final Random random = new Random();
            final int randomMax = 300;
            final int randomMin = 250;
            final int maxCount = 350;

            HashMap<String, Object> headers = new HashMap<>();
            headers.put("Cookie", cookie);

            java.util.List<Long> signedSongList = new ArrayList<>();
            HashMap<Long, Integer> signedSongMap, signedListMap = SignDao.getInstance(context).getList(userId);
            String result, url, params, param = "{\"logs\":\"[{\\\"action\\\":\\\"play\\\",\\\"json\\\":{\\\"sourceId\\\":\\\"%s\\\",\\\"type\\\":\\\"song\\\",\\\"wifi\\\":0,\\\"download\\\":0,\\\"id\\\":%s,\\\"time\\\":%s,\\\"end\\\":\\\"ui\\\"}}]\",\"csrf_token\":\"\"}";
            Gson gson = new Gson();
            int count = 0;

            DailyRecommend dailyRecommend = gson.fromJson(new Http("GET", "https://music.163.com/api/v1/discovery/recommend/resource", null, headers).getResult(), DailyRecommend.class);
            Start:
            for (DailyRecommend.RecommendBean recommendBean : dailyRecommend.getRecommend()) {
                if (signedListMap.get(recommendBean.getId()) != null)
                    continue;
                signedSongMap = SignDao.getInstance(context).getSong(recommendBean.getId(), userId);
                PlaylistDetail playlistDetail = gson.fromJson(new Http("GET", "https://music.163.com/api/v1/playlist/detail?id=" + recommendBean.getId(), null, headers).getResult(), PlaylistDetail.class);
                for (PlaylistDetail.PlaylistBean.TrackIdsBean trackIdsBean : playlistDetail.getPlaylist().getTrackIds()) {
                    if (signedSongMap.get(trackIdsBean.getId()) != null)
                        continue;
                    url = "https://music.163.com/weapi/feedback/weblog?csrf_token=";
                    params = String.format(param, playlistDetail.getPlaylist().getId() + "", trackIdsBean.getId() + "", (random.nextInt(randomMax) % (randomMax - randomMin + 1) + randomMin) + "");
                    try {
                        params = "params=" + URLEncoder.encode(NeteaseAES.get_params(params), "UTF-8") + "&encSecKey="
                                + NeteaseAES.get_encSecKey();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result = new Http("POST", url, headers, params).getResult();
                    if (result.contains("success")) {
                        signedSongList.add(trackIdsBean.getId());
                        count++;
                    }
                    if (count >= maxCount) {
                        SignDao.getInstance(context).saveSongList(signedSongList, recommendBean.getId(), userId);
                        break Start;
                    }
                }
                SignDao.getInstance(context).saveList(recommendBean.getId(), userId);
                SignDao.getInstance(context).deleteSong(recommendBean.getId(), userId);
                signedSongList.clear();
            }
            log("打卡完毕，共计打卡歌曲" + count + "首！");
            Tools.showToastOnLooper(context, "打卡完毕，共计打卡歌曲" + count + "首！");
            Tools.copyFile(Tools.neteaseDbPath, Tools.sdcardDbPath);
            Tools.copyFile(Tools.neteaseDbPath + "-journal", Tools.sdcardDbPath + "-journal");
            ExtraDao.getInstance(context).saveExtra("signSongDate" + userId, signSongDate);
        }).start();
    }

    /**
     * 获取今天0点的时间戳
     *
     * @return
     */
    public static long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime();
    }
}
