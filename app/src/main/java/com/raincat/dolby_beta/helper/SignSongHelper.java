package com.raincat.dolby_beta.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.raincat.dolby_beta.db.SignDao;
import com.raincat.dolby_beta.model.DailyRecommendListBean;
import com.raincat.dolby_beta.model.PlaylistDetailBean;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES;
import com.raincat.dolby_beta.utils.Tools;
import com.raincat.dolby_beta.view.BaseDialogItem;
import com.raincat.dolby_beta.view.BaseDialogTextItem;
import com.raincat.dolby_beta.view.sign.SignCountView;
import com.raincat.dolby_beta.view.sign.SignIdView;
import com.raincat.dolby_beta.view.sign.SignStartView;
import com.raincat.dolby_beta.view.sign.SignTitleView;
import com.raincat.tools.nettools.HttpConfig;
import com.raincat.tools.nettools.NetCallBack;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/13
 *     desc   : 歌曲打卡帮助类
 *     version: 1.0
 * </pre>
 */

public class SignSongHelper {
    private static final String URL = "https://music.163.com/weapi/feedback/weblog?csrf_token=";
    private static final String PARAM = "{\"logs\":\"[{\\\"action\\\":\\\"play\\\",\\\"json\\\":{\\\"sourceId\\\":\\\"%s\\\",\\\"type\\\":\\\"song\\\",\\\"wifi\\\":0,\\\"download\\\":0,\\\"id\\\":%s,\\\"time\\\":%s,\\\"end\\\":\\\"ui\\\"}}]\",\"csrf_token\":\"\"}";
    private static final Random RANDOM = new Random();
    private static final int RANDOM_MAX = 300;
    private static final int RANDOM_MIN = 250;

    private static final String PLAYLIST = "获取到%s个歌单列表，正在获取第%s个歌单详情……";
    private static final String SONG = "歌单“%s”共有%s首歌……";
    private static final String SIGN = "正在打卡第%s首歌，忽略往日已打歌曲%s首，共成功%s首……";
    private static final String FINISH = "打卡完成，请点击确定退出打卡！";

    private static final String DAILY_FAIL = "获取每日推荐歌单列表失败！";
    private static final String FAIL = "打卡失败，请点击确定退出打卡！";

    private static DailyRecommendListBean dailyRecommendListBean;
    private static PlaylistDetailBean playlistDetailBean;
    private static HashMap<String, Object> headers;
    //历史打卡歌曲id
    private static HashMap<Long, Integer> historySignMap;
    //当前已打卡歌曲id
    private static List<Long> signedList;
    private static Gson gson = new Gson();
    //打卡状态
    private static boolean signing = false;
    //当前打卡的歌单索引，歌曲索引，忽略个数，成功个数
    private static int playListIndex = 0, songIndex = 0, ignoreSignCount = 0, successSignCount = 0;
    //最大打卡个数
    private static int maxCount = 350;

    public static void showSelfSignDialog(final Context context) {
        init(context);

        LinearLayout dialogSignRoot = new BaseDialogItem(context);
        dialogSignRoot.setOrientation(LinearLayout.VERTICAL);
        SignTitleView signTitleView = new SignTitleView(context);
        signTitleView.setTitle(SettingHelper.sign_self_title);
        dialogSignRoot.addView(signTitleView);
        dialogSignRoot.addView(new SignIdView(context));
        dialogSignRoot.addView(new SignStartView(context));
        dialogSignRoot.addView(new SignCountView(context));

        new AlertDialog.Builder(context)
                .setView(dialogSignRoot)
                .setCancelable(true)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    Pattern pattern = Pattern.compile("playlist/\\d+");
                    Matcher matcher = pattern.matcher(SettingHelper.getInstance().getSignId());
                    Pattern pattern2 = Pattern.compile("id=\\d+");
                    Matcher matcher2 = pattern2.matcher(SettingHelper.getInstance().getSignId());
                    Pattern pattern3 = Pattern.compile("[\\d]");
                    Matcher matcher3 = pattern3.matcher(SettingHelper.getInstance().getSignId());
                    long playListId = 0;
                    if (matcher.find()) {
                        playListId = Long.parseLong(matcher.group().substring(9));
                    } else if (matcher2.find()) {
                        playListId = Long.parseLong(matcher2.group().substring(3));
                    } else if (matcher3.replaceAll("").length() == 0) {
                        playListId = Long.parseLong(SettingHelper.getInstance().getSignId());
                    } else
                        Tools.showToastOnLooper(context, "歌单ID识别失败！");

                    if (playListId != 0) {
                        dailyRecommendListBean = new DailyRecommendListBean();
                        DailyRecommendListBean.RecommendBean recommendBean = new DailyRecommendListBean.RecommendBean();
                        recommendBean.setId(playListId);
                        recommendBean.setName(playListId + "");
                        dailyRecommendListBean.getRecommend().add(recommendBean);
                        maxCount = SettingHelper.getInstance().getSignCount();
                        songIndex = SettingHelper.getInstance().getSignStart();
                        songIndex = (songIndex == 0 ? 0 : songIndex - 1);
                        showSignStatusDialog(context, SettingHelper.sign_self_title, dailyRecommendListBean);
                    } else
                        end(context);
                })
                .setNegativeButton("取消", null).show();
    }

    public static void showSignStatusDialog(Context context, String title, DailyRecommendListBean bean) {
        init(context);

        LinearLayout dialogRoot = new BaseDialogItem(context);
        SignTitleView signTitleView = new SignTitleView(context);
        signTitleView.setTitle(title);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        dialogRoot.addView(signTitleView);
        final BaseDialogTextItem[] baseDialogTextItems = new BaseDialogTextItem[4];
        for (int i = 0; i < baseDialogTextItems.length; i++) {
            baseDialogTextItems[i] = new BaseDialogTextItem(context);
            dialogRoot.addView(baseDialogTextItems[i]);
        }
        new AlertDialog.Builder(context)
                .setView(dialogRoot)
                .setCancelable(false)
                .setPositiveButton("确定", (dialogInterface, i) -> signing = false).show();

        if (bean == null)
            dailyRecommendListBean = getDailyRecommendList();
        if (dailyRecommendListBean.getRecommend().size() != 0) {
            getPlayListDetail(context, baseDialogTextItems);
        } else {
            baseDialogTextItems[0].setText(DAILY_FAIL);
            baseDialogTextItems[1].setText(FAIL);
            baseDialogTextItems[0].setVisibility(View.VISIBLE);
            baseDialogTextItems[1].setVisibility(View.VISIBLE);
            end(context);
        }
    }


    private static DailyRecommendListBean getDailyRecommendList() {
        return gson.fromJson(new Http("GET", "https://music.163.com/api/v1/discovery/recommend/resource",
                null, headers).getResult(), DailyRecommendListBean.class);
    }

    private static void getPlayListDetail(Context context, BaseDialogTextItem[] items) {
        if (playListIndex < dailyRecommendListBean.getRecommend().size()) {
            items[0].setText(String.format(PLAYLIST, dailyRecommendListBean.getRecommend().size(), playListIndex + 1));
            items[0].setVisibility(View.VISIBLE);

            new HttpConfig().doGet("https://music.163.com/api/v1/playlist/detail?id=" + dailyRecommendListBean.getRecommend().get(playListIndex).getId())
                    .headers(headers).start(context, new NetCallBack() {
                @Override
                public void finish(JSONObject jsonObject) {
                    playlistDetailBean = gson.fromJson(jsonObject.toString(), PlaylistDetailBean.class);

                    if (playlistDetailBean.getPlaylist().getTrackIds().size() != 0 && signing) {
                        items[1].setText(String.format(SONG, dailyRecommendListBean.getRecommend().get(playListIndex++).getName(), playlistDetailBean.getPlaylist().getTrackIds().size()));
                        items[1].setVisibility(View.VISIBLE);
                        signSong(context, songIndex, playlistDetailBean.getPlaylist().getTrackIds(), items);
                    } else if (signing)
                        getPlayListDetail(context, items);
                    else {
                        items[3].setText(FINISH);
                        items[3].setVisibility(View.VISIBLE);
                        end(context);
                    }
                }

                @Override
                public void error(int i, String s) {
                    items[3].setText(FINISH);
                    items[3].setVisibility(View.VISIBLE);
                    end(context);
                }
            });
        } else {
            items[3].setText(FINISH);
            items[3].setVisibility(View.VISIBLE);
            end(context);
        }
    }

    private static void signSong(Context context, int index, List<PlaylistDetailBean.PlaylistBean.TrackIdsBean> trackIdsBeanList, BaseDialogTextItem[] items) {
        if (!signing)
            return;

        if (index >= trackIdsBeanList.size()) {
            getPlayListDetail(context, items);
        } else if (historySignMap.get(trackIdsBeanList.get(index).getId()) != null) {
            items[2].setText(String.format(SIGN, index + 1, ++ignoreSignCount, successSignCount));
            items[2].setVisibility(View.VISIBLE);
            signSong(context, index + 1, trackIdsBeanList, items);
        } else {
            String localParam = String.format(PARAM, playlistDetailBean.getPlaylist().getId() + "", trackIdsBeanList.get(index).getId() + "", (RANDOM.nextInt(RANDOM_MAX) % (RANDOM_MAX - RANDOM_MIN + 1) + RANDOM_MIN) + "");
            try {
                localParam = "params=" + URLEncoder.encode(NeteaseAES.get_params(localParam), "UTF-8") + "&encSecKey=" + NeteaseAES.get_encSecKey();
            } catch (Exception e) {
                e.printStackTrace();
            }

            new HttpConfig().doPost(URL, localParam).headers(headers).start(context, new NetCallBack() {
                @Override
                public void finish(JSONObject jsonObject) {
                    if (jsonObject.toString().contains("success")) {
                        signedList.add(trackIdsBeanList.get(index).getId());
                        successSignCount++;
                    }

                    items[2].setText(String.format(SIGN, index + 1, ignoreSignCount, successSignCount));
                    items[2].setVisibility(View.VISIBLE);

                    if (successSignCount >= maxCount) {
                        items[3].setText(FINISH);
                        items[3].setVisibility(View.VISIBLE);
                        end(context);
                    } else
                        signSong(context, index + 1, trackIdsBeanList, items);
                }

                @Override
                public void error(int i, String s) {
                    items[3].setText(FAIL);
                    items[3].setVisibility(View.VISIBLE);
                    end(context);
                }
            });
        }
    }

    private static void init(Context context) {
        if (headers == null)
            headers = new HashMap<>();
        headers.put("Cookie", ExtraHelper.getExtraDate(ExtraHelper.COOKIE));

        signing = true;
        historySignMap = SignDao.getInstance(context).getSong(ExtraHelper.getExtraDate(ExtraHelper.USER_ID));
        signedList = new ArrayList<>();
    }

    private static void end(Context context) {
        SignDao.getInstance(context).saveSongList(signedList, ExtraHelper.getExtraDate(ExtraHelper.USER_ID));
        signing = false;
        headers = null;
        historySignMap = null;
        signedList = null;
        dailyRecommendListBean = null;
        playlistDetailBean = null;
        maxCount = 350;
        playListIndex = 0;
        songIndex = 0;
        ignoreSignCount = 0;
        successSignCount = 0;
    }
}
