package com.raincat.dolby_beta.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/14
 *     desc   : 设置中心
 *     version: 1.0
 * </pre>
 */

public class SettingHelper {
    public static final String refresh_setting = "β_refresh_setting";
    public static final String proxy_setting = "β_proxy_setting";
    public static final String beauty_setting = "β_beauty_setting";
    public static final String sidebar_setting = "β_sidebar_setting";

    public static final String master_key = "β_master_key";
    public static final String master_title = "总开关";

    public static final String dex_key = "β_dex_key";
    public static final String dex_title = "启用DEX缓存";
    public static final String dex_sub = "加快模块加载速度，但同版本号的内测版与稳定版互装可能会有兼容性问题";

    public static final String warn_key = "β_warn_key";
    public static final String warn_title = "开启Hook警告";
    public static final String warn_sub = "当模块出现部分类无法Hook时在通知栏上显示，方便定位排查问题";

    public static final String black_key = "β_black_key";
    public static final String black_title = "本地黑胶";
    public static final String black_sub = "去广告、鲸云音效、个性换肤等（自定义启动图等需要访问网易服务器的设置不可用）";

    public static final String listen_key = "β_listen_key";
    public static final String listen_title = "解锁一起听蒙面查看权限";
    public static final String listen_sub = "开启后可直接查看对方信息，无需对方解除蒙面（暂时只支持8.6.45-8.7.70）";

    public static final String fix_comment_key = "β_fix_comment_key";
    public static final String fix_comment_title = "修复评论区加载失败";
    public static final String fix_comment_sub = "如平时不看评论区或评论区无问题请勿打钩";

    public static final String update_key = "β_update_key";
    public static final String update_title = "隐藏升级提示";

    public static final String sign_key = "β_sign_key";
    public static final String sign_title = "自动签到";

    public static final String sign_song_key = "β_sign_song_key";
    public static final String sign_song_title = "每日歌曲打卡";
    public static final String sign_song_sub = "获取每日推荐歌单中的歌曲进行打卡，有利于提高等级，会影响年度听歌总结且有可能会导致几天内签到天数不变，介意者慎用";

    public static final String sign_self_title = "自助打卡";

    public static final String sign_id_key = "β_sign_id_key";
    public static final String sign_id_title = "打卡歌单URL";

    public static final String sign_start_key = "β_sign_start_key";
    public static final String sign_start_title = "期望从第几首歌开始打？";
    public static final int sign_start_default = 1;

    public static final String sign_count_key = "β_sign_count_key";
    public static final String sign_count_title = "期望打卡多少首歌？";
    public static final int sign_count_default = 300;

    public static final String proxy_key = "β_proxy_key";
    public static final String proxy_title = "音源代理设置";

    public static final String proxy_master_key = "β_proxy_master_key";
    public static final String proxy_master_title = "代理开关";

    public static final String proxy_server_key = "β_proxy_server_key";
    public static final String proxy_server_title = "服务器代理模式";
    public static final String proxy_server_sub = "如果您不想使用高占用的node，有自己的服务器代理可使用此方式并填写自己的服务器地址与端口，且使用服务器对应音质";

    public static final String proxy_priority_key = "β_proxy_priority_key";
    public static final String proxy_priority_title = "音质优先";
    public static final String proxy_priority_sub = "音质优先：使用外部音源提高音质，不可避免的会增大匹配错误概率\n匹配度优先：尽可能采用网易云音源，但非会员很多曲目只有128K/96K";

    public static final String proxy_flac_key = "β_proxy_flac_key";
    public static final String proxy_flac_title = "无损音质优先";
    public static final String proxy_flac_sub = "使用外部音源时优先获取无损音质，但并不是100%能获取到无损音质";

    public static final String proxy_gray_key = "β_proxy_gray_key";
    public static final String proxy_gray_title = "不变灰";
    public static final String proxy_gray_sub = "只影响显示效果，与能否播放无关，会导致无音源歌曲无法播放且无法自动跳过";

    public static final String http_proxy_key = "β_http_proxy_key";
    public static final String http_proxy_title = "代理服务器";
    public static final String http_proxy_default = "127.0.0.1";

    public static final String proxy_port_key = "β_proxy_port_key";
    public static final String proxy_port_title = "代理端口（1~65535）";
    public static final int proxy_port_default = 23338;

    public static final String proxy_original_key = "β_proxy_original_key";
    public static final String proxy_original_title = "代理源（空格隔开）";
    public static final String proxy_original_default = "kuwo qq kugou";

    public static final String proxy_cover_key = "β_proxy_cover_key";
    public static final String proxy_cover_title = "重新释放脚本";
    public static final String proxy_cover_sub = "当更新后或者发现UnblockNeteaseMusic运行不正常时可尝试重新释放脚本";

    public static final String beauty_key = "β_beauty_key";
    public static final String beauty_title = "美化设置";

    public static final String beauty_night_mode_key = "β_beauty_night_mode_key";
    public static final String beauty_night_mode_title = "跟随系统切换夜间模式";
    public static final String beauty_night_mode_sub = "自动根据系统深色模式状态切换夜间/日间模式";

    public static final String beauty_tab_hide_key = "β_beauty_tab_hide_key";
    public static final String beauty_tab_hide_title = "精简Tab";
    public static final String beauty_tab_hide_sub = "首页仅保留“我的”与“发现”，并默认显示“我的";

    public static final String beauty_bubble_hide_key = "β_beauty_bubble_hide_key";
    public static final String beauty_bubble_hide_title = "移除小红点";

    public static final String beauty_banner_hide_key = "β_beauty_banner_hide_key";
    public static final String beauty_banner_hide_title = "移除发现页与歌单广场Banner";

    public static final String beauty_ksong_hide_key = "β_beauty_ksong_key";
    public static final String beauty_ksong_hide_title = "移除播放页K歌图标";

    public static final String beauty_black_hide_key = "β_beauty_black_key";
    public static final String beauty_black_hide_title = "播放页专辑图片外面的黑胶隐藏";

    public static final String beauty_rotation_key = "β_beauty_rotation_key";
    public static final String beauty_rotation_title = "播放页专辑图片停止转动";

    public static final String beauty_comment_hot_key = "β_beauty_comment_hot_key";
    public static final String beauty_comment_hot_title = "评论区优先显示“最热”内容";

    public static final String beauty_sidebar_hide_key = "β_beauty_sidebar_hide_key";
    public static final String beauty_sidebar_hide_title = "精简侧边栏";
    public static final String beauty_sidebar_hide_sub = "部分Item需配合“设置”->“侧边栏管理”开关生效";

    private static SettingHelper instance;

    private SharedPreferences sharedPreferences;
    private HashMap<String, Boolean> settingMap;
    private HashMap<String, Boolean> sidebarSettingMap;

    public static SettingHelper getInstance() {
        return instance;
    }

    private SettingHelper(Context context) {
        refreshSetting(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SettingHelper(context);
        }
    }

    public void refreshSetting(Context context) {
        sharedPreferences = context.getSharedPreferences("com.netease.cloudmusic.preferences", Context.MODE_MULTI_PROCESS);
        settingMap = new HashMap<>();

        settingMap.put(master_key, sharedPreferences.getBoolean(master_key, true));
        settingMap.put(dex_key, sharedPreferences.getBoolean(dex_key, true));
        settingMap.put(warn_key, sharedPreferences.getBoolean(warn_key, true));
        settingMap.put(black_key, sharedPreferences.getBoolean(black_key, true));
        settingMap.put(listen_key, sharedPreferences.getBoolean(listen_key, false));
        settingMap.put(fix_comment_key, sharedPreferences.getBoolean(fix_comment_key, false));
        settingMap.put(update_key, sharedPreferences.getBoolean(update_key, true));
        settingMap.put(sign_key, sharedPreferences.getBoolean(sign_key, true));
        settingMap.put(sign_song_key, sharedPreferences.getBoolean(sign_song_key, false));

        settingMap.put(proxy_master_key, sharedPreferences.getBoolean(proxy_master_key, true));
        settingMap.put(proxy_server_key, sharedPreferences.getBoolean(proxy_server_key, false));
        settingMap.put(proxy_priority_key, sharedPreferences.getBoolean(proxy_priority_key, false));
        settingMap.put(proxy_flac_key, sharedPreferences.getBoolean(proxy_flac_key, false));
        settingMap.put(proxy_gray_key, sharedPreferences.getBoolean(proxy_gray_key, false));

        settingMap.put(beauty_night_mode_key, sharedPreferences.getBoolean(beauty_night_mode_key, false));
        settingMap.put(beauty_tab_hide_key, sharedPreferences.getBoolean(beauty_tab_hide_key, false));
        settingMap.put(beauty_bubble_hide_key, sharedPreferences.getBoolean(beauty_bubble_hide_key, false));
        settingMap.put(beauty_banner_hide_key, sharedPreferences.getBoolean(beauty_banner_hide_key, false));
        settingMap.put(beauty_ksong_hide_key, sharedPreferences.getBoolean(beauty_ksong_hide_key, false));
        settingMap.put(beauty_rotation_key, sharedPreferences.getBoolean(beauty_rotation_key, false));
        settingMap.put(beauty_black_hide_key, sharedPreferences.getBoolean(beauty_black_hide_key, false));
        settingMap.put(beauty_comment_hot_key, sharedPreferences.getBoolean(beauty_comment_hot_key, false));
    }

    public void setSetting(String key, boolean value) {
        settingMap.put(key, value);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getSetting(String key) {
        return settingMap.get(key);
    }

    public boolean isEnable(String key) {
        return settingMap.get(master_key) && settingMap.get(key);
    }

    public HashMap<String, Boolean> getSidebarSetting(LinkedHashMap<String, String> map) {
        if (sidebarSettingMap == null) {
            sidebarSettingMap = new HashMap<>();
            for (String key : map.keySet()) {
                sidebarSettingMap.put(key, sharedPreferences.getBoolean(key, false));
            }
        }
        return sidebarSettingMap;
    }

    public void setSidebarSetting(String key, boolean value) {
        sidebarSettingMap.put(key, value);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public String getSignId() {
        return sharedPreferences.getString(SettingHelper.sign_id_key, "");
    }

    public void setSignId(String id) {
        if (!TextUtils.isEmpty(id))
            sharedPreferences.edit().putString(SettingHelper.sign_id_key, id).apply();
    }

    public int getSignStart() {
        return sharedPreferences.getInt(SettingHelper.sign_start_key, SettingHelper.sign_start_default);
    }

    public void setSignStart(String start) {
        if (!TextUtils.isEmpty(start))
            sharedPreferences.edit().putInt(SettingHelper.sign_start_key, Integer.parseInt(start)).apply();
    }

    public int getSignCount() {
        return sharedPreferences.getInt(SettingHelper.sign_count_key, SettingHelper.sign_count_default);
    }

    public void setSignCount(String start) {
        if (!TextUtils.isEmpty(start))
            sharedPreferences.edit().putInt(SettingHelper.sign_count_key, Integer.parseInt(start)).apply();
    }

    public int getProxyPort() {
        return sharedPreferences.getInt(SettingHelper.proxy_port_key, SettingHelper.proxy_port_default);
    }

    public void setProxyPort(String port) {
        if (!TextUtils.isEmpty(port))
            sharedPreferences.edit().putInt(SettingHelper.proxy_port_key, Integer.parseInt(port)).apply();
    }

    public String getProxyOriginal() {
        return sharedPreferences.getString(SettingHelper.proxy_original_key, SettingHelper.proxy_original_default);
    }

    public void setProxyOriginal(String original) {
        if (!TextUtils.isEmpty(original))
            sharedPreferences.edit().putString(SettingHelper.proxy_original_key, original).apply();
    }

    public void setHttpProxy(String http) {
        if (!TextUtils.isEmpty(http))
            sharedPreferences.edit().putString(SettingHelper.http_proxy_key, http).apply();
    }

    public String getHttpProxy() {
        return sharedPreferences.getString(SettingHelper.http_proxy_key, SettingHelper.http_proxy_default);
    }
}
