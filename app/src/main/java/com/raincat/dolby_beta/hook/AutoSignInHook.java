package com.raincat.dolby_beta.hook;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.google.gson.Gson;
import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.db.SignDao;
import com.raincat.dolby_beta.model.DailyRecommend;
import com.raincat.dolby_beta.model.PlaylistDetail;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/12/05
 *     desc   : 自动签到hook
 *     version: 1.0
 * </pre>
 */

public class AutoSignInHook {
    private TextView drawerUserSignIn;
    private Object mainDrawer;

    private Calendar calendar;
    private long nextSignInTime = 0L;
    private static boolean nextSingInFlag = false;

    private String classMainDrawer = "com.netease.cloudmusic.ui.MainDrawer";
    private String methodInitDrawerHeader = "initDrawerHeader";
    private String methodUpdateSignIn = "updateSignIn";
    private String methodToggleDrawerMenu = "toggleDrawerMenu";
    private String methodIsDoingSinginTask = "isDoingSinginTask";
    private String methodDoSignInTask = "doSignInTask";
    private String valueDrawerUserSignIn = "drawerUserSignIn";

    public AutoSignInHook(Context context, int versionCode) {
        if (versionCode < 138) {
            classMainDrawer = "com.netease.cloudmusic.ui.l";
            methodInitDrawerHeader = "r";
            methodUpdateSignIn = "a";
            methodToggleDrawerMenu = "d";
            methodIsDoingSinginTask = "h";
            methodDoSignInTask = "i";
            valueDrawerUserSignIn = "t";

            if (findClassIfExists("com.netease.cloudmusic.activity.ReactNativeActivity", context.getClassLoader()) != null) {
                //禁止签到跳转到商城
                findAndHookMethod("com.netease.cloudmusic.activity.ReactNativeActivity", context.getClassLoader(),
                        "a", Context.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                Object context = param.args[0];
                                if (context.getClass().getName().equals("com.netease.cloudmusic.activity.MainActivity")) {
                                    param.setResult(null);
                                }
                            }
                        });
            }
        } else {
            //禁止签到跳转到商城
            if (findClassIfExists("com.netease.cloudmusic.activity.ReactNativeActivity", context.getClassLoader()) != null) {
                XposedBridge.hookAllMethods(findClass("com.netease.cloudmusic.activity.ReactNativeActivity", context.getClassLoader()), "a", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.args.length >= 3 && param.args[0] instanceof Context) {
                            if (param.args[1] instanceof Boolean && param.args[2] instanceof String) {
                                Object context = param.args[0];
                                boolean b = (boolean) param.args[1];
                                if (context.getClass().getName().equals("com.netease.cloudmusic.activity.MainActivity") && b) {
                                    param.setResult(null);
                                }
                            } else if (param.args[1] instanceof String && param.args[2] instanceof Boolean) {
                                Object context = param.args[0];
                                String shell = (String) param.args[1];
                                if (shell.equals("ReactNativeStore") || shell.toLowerCase().contains("shell") || shell.toLowerCase().contains("center")) {
                                    if (context.getClass().getName().equals("com.netease.cloudmusic.activity.RedirectActivity")) {
                                        ((Activity) context).finish();
                                        param.setResult(null);
                                    } else if (context.getClass().getName().equals("com.netease.cloudmusic.activity.MainActivity"))
                                        param.setResult(null);
                                }
                            }
                        }
                    }
                });
            }
        }

        //获取关键Object
        findAndHookMethod(classMainDrawer, context.getClassLoader(), methodInitDrawerHeader, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mainDrawer = param.thisObject;
                Field drawerUserSignInField = mainDrawer.getClass().getDeclaredField(valueDrawerUserSignIn);
                drawerUserSignInField.setAccessible(true);
                drawerUserSignIn = (TextView) drawerUserSignInField.get(mainDrawer);
            }
        });

        //修改签到UI
        findAndHookMethod(classMainDrawer, context.getClassLoader(), methodUpdateSignIn, boolean.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                boolean bool0 = (boolean) param.args[0];
                boolean bool1 = (boolean) param.args[1];

                if (!bool0 && bool1) {
                    sign(context);
                }
                drawerUserSignIn.setText("已签到");
                drawerUserSignIn.setEnabled(false);
                drawerUserSignIn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        });

        //禁止签到的时候打开侧边栏
        findAndHookMethod(classMainDrawer, context.getClassLoader(), methodToggleDrawerMenu,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (nextSingInFlag) {
                            param.setResult(null);
                        }
                    }
                });

        //更改当前签到状态
        findAndHookMethod("com.netease.cloudmusic.meta.Profile", context.getClassLoader(),
                "isMobileSign", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (nextSingInFlag) {
                            param.setResult(false);
                        }
                    }
                });

        //每天0点签到
        findAndHookMethod("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader(),
                "onStart", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        calendar = Calendar.getInstance();
                        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
                        calendar.setTimeZone(timeZone);
                        long nowTime = calendar.getTimeInMillis();

                        if (nowTime > nextSignInTime) {
                            sign(context);
                        }
                    }
                });

    }

    private void sign(Context context) {
        if (mainDrawer != null && drawerUserSignIn != null) {
            boolean running = (boolean) callMethod(mainDrawer, methodIsDoingSinginTask);
            if (!running) {
                nextSingInFlag = true;
                callMethod(mainDrawer, methodDoSignInTask);
                nextSingInFlag = false;

                if (calendar == null) {
                    calendar = Calendar.getInstance();
                    TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
                    calendar.setTimeZone(timeZone);
                }

                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                nextSignInTime = calendar.getTimeInMillis() + 86400000;

                signByWeb(context);
                signSong(context);
            }
        }
    }

    private void signByWeb(Context context) {
        HashMap<String, Object> header = new HashMap<>();
        header.put("Cookie", ExtraDao.getInstance(context).getExtra("cookie"));

        HashMap<String, Object> param = new HashMap<>();
        param.put("type", "1");

        new Http("POST", "http://music.163.com/api/point/dailyTask", param, header).getResult();
    }

    private void signSong(Context context) {
        if (!Setting.isSignSongEnabled())
            return;

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
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
            headers.put("Cookie", ExtraDao.getInstance(context).getExtra("cookie"));

            List<Long> signedSongList = new ArrayList<>();
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
            XposedBridge.log("打卡完毕，共计打卡歌曲" + count + "首！");
            Tools.showToastOnLooper(context, "打卡完毕，共计打卡歌曲" + count + "首！");
            Tools.copyFile(Tools.neteaseDbPath, Tools.sdcardDbPath);
            Tools.copyFile(Tools.neteaseDbPath + "-journal", Tools.sdcardDbPath + "-journal");
            ExtraDao.getInstance(context).saveExtra("signSongDate" + userId, signSongDate);
        }).start();
    }
}
