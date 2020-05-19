package com.raincat.dolby_beta.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.raincat.dolby_beta.R;
import com.raincat.dolby_beta.model.PlaylistDetail;
import com.raincat.dolby_beta.net.Http;
import com.raincat.dolby_beta.utils.NeteaseAES;
import com.raincat.tools.nettools.HttpConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/19
 *     desc   : 签到弹窗
 *     version: 1.0
 * </pre>
 */
public class SignSongDialog {
    private static final String warning = "注意：网易云每天仅记录300首歌的听歌量且需要听你从未播放过的歌才会增加听歌量，如果打卡的歌曲和你的听歌习惯不同可能会导致每日推荐与私人FM歌曲紊乱！";
    private static AlertDialog dialog;
    private static int index = 0;
    private static int count = 0;
    private static final String signUrl = "https://music.163.com/weapi/feedback/weblog?csrf_token=";
    private static final String signParam = "{\"logs\":\"[{\\\"action\\\":\\\"play\\\",\\\"json\\\":{\\\"sourceId\\\":\\\"%s\\\",\\\"type\\\":\\\"song\\\",\\\"wifi\\\":0,\\\"download\\\":0,\\\"id\\\":%s,\\\"time\\\":%s,\\\"end\\\":\\\"ui\\\"}}]\",\"csrf_token\":\"\"}";
    private static final Pattern REX_PL = Pattern.compile("playlist/\\d+");

    public static void showSignSongDialog(Context context, final SharedPreferences share) {
        if (dialog != null)
            return;

        String cookie = share.getString("Cookie", "");
        String playlist_id = share.getString("playlist_id", "");
        int start = share.getInt("start", 0) + 1;
        int end = share.getInt("end", 299) + 1;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_signsong_item, null);
        final EditText et_cookie = view.findViewById(R.id.et_cookie);
        final EditText et_id = view.findViewById(R.id.et_id);
        final EditText et_start = view.findViewById(R.id.et_start);
        final EditText et_end = view.findViewById(R.id.et_end);
        final ImageView iv_cookie = view.findViewById(R.id.iv_cookie);
        final ImageView iv_id = view.findViewById(R.id.iv_id);
        et_cookie.setText(cookie);
        et_id.setText(playlist_id);
        et_start.setText(start + "");
        et_end.setText(end + "");

        iv_cookie.setOnClickListener((v) -> {
            et_cookie.setFocusableInTouchMode(true);
            et_cookie.setFocusable(true);
            et_cookie.requestFocus();
            et_cookie.setText("");
        });
        iv_id.setOnClickListener((v) -> {
            et_id.setFocusableInTouchMode(true);
            et_id.setFocusable(true);
            et_id.requestFocus();
            et_id.setText("");
        });

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setTitle("自助打卡")
                .setMessage(warning)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialogInterface, i) -> dialog = null).show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String cookieString = et_cookie.getText().toString();
            String idString = et_id.getText().toString();
            String startString = et_start.getText().toString();
            String endString = et_end.getText().toString();

            EditText view1 = null;
            String error = null;
            if (cookieString.length() == 0) {
                view1 = et_cookie;
                error = "不能为空";
            } else if (idString.length() == 0) {
                view1 = et_id;
                error = "不能为空";
            } else if (startString.length() == 0) {
                view1 = et_start;
                error = "不能为空";
            } else if (endString.length() == 0) {
                view1 = et_end;
                error = "不能为空";
            } else if (startString.equals("0")) {
                view1 = et_start;
                error = "不能为0";
            } else if (endString.equals("0")) {
                view1 = et_end;
                error = "不能为0";
            }

            if (view1 != null) {
                view1.setFocusableInTouchMode(true);
                view1.setFocusable(true);
                view1.requestFocus();
                view1.setError(error);
                return;
            }

            share.edit().putString("Cookie", cookieString).apply();
            share.edit().putString("playlist_id", idString).apply();
            share.edit().putInt("start", Integer.parseInt(startString) - 1).apply();
            share.edit().putInt("end", Integer.parseInt(endString) - 1).apply();

            dialog.dismiss();
            showSignSongStatusDialog(context, share);
        });
    }

    private static void showSignSongStatusDialog(Context context, final SharedPreferences share) {
        String cookie = share.getString("Cookie", "");
        String playlist_id = share.getString("playlist_id", "");

        Matcher matcher = REX_PL.matcher(playlist_id);
        if (matcher.find())
            playlist_id = matcher.group().substring(9);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_signsongstatus_item, null);
        final TextView tv_status = view.findViewById(R.id.tv_status);

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setTitle("自助打卡")
                .setMessage(warning)
                .setPositiveButton("取消", (dialogInterface, i) -> {
                    index = -999;
                    dialog = null;
                }).show();

        HashMap<String, Object> headers = new HashMap<>();
        headers.put("Cookie", cookie);

        Gson gson = new Gson();
        PlaylistDetail playlistDetail = gson.fromJson(new Http("GET", "https://music.163.com/api/v1/playlist/detail?id=" + playlist_id, null, headers).getResult(), PlaylistDetail.class);
        tv_status.append("\n获取成功，本歌单共有" + playlistDetail.getPlaylist().getTrackIds().size() + "首歌！");
        tv_status.append("\n正在打卡……");

        String message = tv_status.getText().toString() + "\n已打%s首歌……";
        final Random random = new Random();
        index = share.getInt("start", 0);
        count = 0;
        signSong(context, tv_status, random, playlistDetail.getPlaylist().getTrackIds(), headers, message, playlist_id, share.getInt("end", 299));
    }

    private static void signSong(Context context, TextView view, Random random, List<PlaylistDetail.PlaylistBean.TrackIdsBean> trackIdsBeanList, HashMap<String, Object> headers, String message, String playListId, int end) {
        if (index < 0)
            return;
        else if (trackIdsBeanList.size() <= index || end < index) {
            view.append("\n打卡完毕，点击取消退出本次打卡！");
            return;
        }
        String cryptoParam = String.format(signParam, playListId + "", trackIdsBeanList.get(index).getId() + "", (random.nextInt(300) % (300 - 250 + 1) + 250) + "");
        try {
            cryptoParam = "params=" + URLEncoder.encode(NeteaseAES.get_params(cryptoParam), "UTF-8") + "&encSecKey="
                    + NeteaseAES.get_encSecKey();
        } catch (Exception e) {
            view.append("\n打卡失败，点击取消退出本次打卡！");
            return;
        }

        new HttpConfig().doPost(signUrl, cryptoParam).headers(headers).start(context, new com.raincat.tools.nettools.NetCallBack() {
            @Override
            public void finish(JSONObject jsonObject) throws JSONException {
                if (jsonObject.getInt("code") == 200) {
                    index++;
                    count++;
                    view.setText(String.format(message, count));
                    signSong(context, view, random, trackIdsBeanList, headers, message, playListId, end);
                } else {
                    view.append("\n打卡失败，点击取消退出本次打卡！");
                }
            }

            @Override
            public void error(int i, String s) {
                view.append("\n打卡失败，点击取消退出本次打卡！");
            }
        });
    }
}
