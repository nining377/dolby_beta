package com.raincat.dolby_beta.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/27
 *     desc   : 说明
 *     version: 1.0
 * </pre>
 */
public class SidebarCutDialog {
    private static AlertDialog dialog;

    public static void showSidebarCutDialog(Context context, final SharedPreferences share) {
        if (dialog != null)
            return;

        boolean[] checked = new boolean[itemsMap.size()];
        CharSequence[] items = new CharSequence[itemsMap.size()];
        int index = 0;
        Set<Map.Entry<String, String>> entrySet = itemsMap.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            items[index] = entry.getValue();
            checked[index++] = share.getBoolean(entry.getKey(), false);
        }

        dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("侧边栏精简")
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    int index2 = 0;
                    Set<String> keys = itemsMap.keySet();
                    for (String key : keys) {
                        share.edit().putBoolean(key, checked[index2++]).apply();
                    }
                    dialog = null;
                }).setNegativeButton("取消", (dialogInterface, i) -> {
                    dialog = null;
                }).setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                }).show();
    }

    private static final LinkedHashMap itemsMap = new LinkedHashMap() {{
        put("MUSICIAN", "音乐人中心(6.0.0)");

        put("MUSICIAN_CREATOR_CENTER", "创作者中心");
        put("IDENTIFY", "听歌识曲");
        put("TICKET", "演出");
        put("NEARBY", "附近的人");

        put("CLOCK_PLAY", "定时停止播放");
        put("SCAN", "扫一扫");
        put("PRIVATE_CLOUD", "音乐云盘");
        put("YOUTH_MODE", "青少年模式");

        put("div2", "分割线2");
        put("div3", "分割线3");
        put("VIP_CARD", "黑胶卡片");

        put("STORE", "商城(侧边栏管理)");
        put("GAME", "游戏推荐(侧边栏管理)");
        put("ALARM_CLOCK", "音乐闹钟(侧边栏管理)");
        put("FREE", "在线听歌免流量(侧边栏管理)");
        put("DISCOUNT_COUPON", "优惠券(侧边栏管理)");
        put("COLOR_RING", "口袋彩铃(侧边栏管理)");
        put("MY_ORDER", "我的订单(侧边栏管理)");

        put("MESSAGE", "我的消息(4.3.1)");
        put("VIP", "会员中心(4.3.1)");
        put("MY_FRIEND", "我的好友(4.3.1)");
        put("THEME", "个性换肤(4.3.1)");
        put("VEHICLE_PLAYER", "驾驶模式(4.3.1)");

        put("MUSICIAN_VIEWER", "加入网易音乐人");
        put("RED_PACKET", "音乐红包");
        put("PROFIT", "赞赏收入");
    }};
}
