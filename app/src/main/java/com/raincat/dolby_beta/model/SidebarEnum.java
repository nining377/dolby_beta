package com.raincat.dolby_beta.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/22
 *     desc   : 侧边栏枚举
 *     version: 1.0
 * </pre>
 */

public class SidebarEnum {
    //所有枚举Map
    private static LinkedHashMap<String, String> enumMap;
    //当前版本枚举Map
    private static LinkedHashMap<String, String> sidebarMap;

    public static void setSidebarEnum(Object[] objectList) {
        sidebarMap = new LinkedHashMap<>();
        HashMap<String, String> tempMap = new HashMap<>();
        for (Object object : objectList) {
            tempMap.put(object.toString(), object.toString());
        }
        for (Map.Entry<String, String> entry : enumMap.entrySet()) {
            if (tempMap.get(entry.getKey()) != null) {
                sidebarMap.put(entry.getKey(), entry.getValue());
                tempMap.remove(entry.getKey());
            }
        }
        if (tempMap.size() != 0) {
            for (String key : tempMap.values()) {
                if (key.equals("SETTING") || key.equals("DYNAMIC_ITEM") || key.equals("PROFILE") || key.equals("CHILD_MODE") ||
                        key.equals("CLASSICAL") || key.equals("AVATAR") || key.equals("DYNAMIC_CONTAINER") || key.equals("SMALL_ICE"))
                    continue;
                if (key.equals("GROUP")) {
                    sidebarMap.put(key + "1", "音乐服务（组）");
                    sidebarMap.put(key + "2", "其他（组）");
                } else
                    sidebarMap.put(key, "");
            }
        }
    }

    public static LinkedHashMap<String, String> getSidebarEnum() {
        return sidebarMap;
    }

    static {
        enumMap = new LinkedHashMap<>();
        enumMap.put("LOGIN", "登录");
        enumMap.put("MESSAGE", "我的消息");
        enumMap.put("VIP", "我的会员");
        enumMap.put("CLOUD_SHELL_CENTER", "云贝中心");
        enumMap.put("MUSICIAN", "音乐人中心");
        enumMap.put("CREATOR_CENTER", "创作者中心");
        enumMap.put("MUSICIAN_CREATOR_CENTER", "创作者中心");
        enumMap.put("MUSICIAN_VIEWER", "加入网易音乐人");
        enumMap.put("TICKET", "云村有票");
        enumMap.put("NEARBY", "附近的人");
        enumMap.put("STORE", "商城");
        enumMap.put("BEAT", "Beat交易平台");
        enumMap.put("GAME", "游戏专区");
        enumMap.put("COLOR_RING", "口袋彩铃");
        enumMap.put("SETTING", "设置");
        enumMap.put("NIGHT_THEME_MODE", "夜间模式");
        enumMap.put("CLOCK_PLAY", "定时停止播放");
        enumMap.put("THEME", "个性装扮");
        enumMap.put("IDENTIFY", "听歌识曲");
        enumMap.put("SCAN", "扫一扫");
        enumMap.put("CACHE_WHILE_LISTEN", "边听边存");
        enumMap.put("FREE", "在线听歌免流量");
        enumMap.put("MUSIC_BLACKLIST", "音乐黑名单");
        enumMap.put("PRIVATE_CLOUD", "音乐云盘");
        enumMap.put("YOUTH_MODE", "青少年模式");
        enumMap.put("ALARM_CLOCK", "音乐闹钟");
        enumMap.put("MY_ORDER", "我的订单");
        enumMap.put("MY_FRIEND", "我的好友");
        enumMap.put("VEHICLE_PLAYER", "驾驶模式");
        enumMap.put("DISCOUNT_COUPON", "优惠券");
        enumMap.put("RED_PACKET", "音乐红包");
        enumMap.put("PROFIT", "赞赏收入");
        enumMap.put("DYNAMIC_ITEM", "第三方隐私协议");
        enumMap.put("FEEDBACK_HELP", "帮助与反馈");
        enumMap.put("SHARE_APP", "分享网易云音乐");
        enumMap.put("ABOUT", "关于");
        enumMap.put("LOGOUT", "登出");
        enumMap.put("DIV1", "分割线1");
        enumMap.put("DIV2", "分割线2");
        enumMap.put("DIV3", "分割线3");
        enumMap.put("DIV4", "分割线4");
    }
}
