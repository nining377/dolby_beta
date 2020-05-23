package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.raincat.dolby_beta.utils.Setting;

import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/27
 *     desc   : 侧边栏精简
 *     version: 1.0
 * </pre>
 */
public class HideSidebarHook {
    private String classMainDrawerString = "com.netease.cloudmusic.ui.MainDrawer";
    private String classDrawerItemEnumString = "com.netease.cloudmusic.ui.MainDrawer$DrawerItemEnum";
    private String methodRefreshDrawerString = "refreshDrawer";
    private String objectMDrawerContainerString = "mDrawerContainer";

    private HashMap<String, Boolean> tagMap = new HashMap<>();


    public HideSidebarHook(Context context, int versionCode) {
        if (versionCode < 138) {
            classMainDrawerString = "com.netease.cloudmusic.ui.l";
            classDrawerItemEnumString = "com.netease.cloudmusic.ui.l$b";
            methodRefreshDrawerString = "m";
            objectMDrawerContainerString = "i";
        }

        //我的消息
        tagMap.put("MESSAGE", Setting.getSidebar("MESSAGE"));
        //音乐人中心
        tagMap.put("MUSICIAN", Setting.getSidebar("MUSICIAN"));
        //赞赏收入
        tagMap.put("PROFIT", Setting.getSidebar("PROFIT"));
        //会员中心
        tagMap.put("VIP", Setting.getSidebar("VIP"));
        //云音乐商城
        tagMap.put("STORE", Setting.getSidebar("STORE"));
        //在线听歌免流量
        tagMap.put("FREE", Setting.getSidebar("FREE"));
        //我的好友
        tagMap.put("MY_FRIEND", Setting.getSidebar("MY_FRIEND"));
        //附近的人
        tagMap.put("NEARBY", Setting.getSidebar("NEARBY"));
        //个性换肤
        tagMap.put("THEME", Setting.getSidebar("THEME"));
        //听歌识曲
        tagMap.put("IDENTIFY", Setting.getSidebar("IDENTIFY"));
        //定时停止播放
        tagMap.put("CLOCK_PLAY", Setting.getSidebar("CLOCK_PLAY"));
        //扫一扫
        tagMap.put("SCAN", Setting.getSidebar("SCAN"));
        //音乐闹钟
        tagMap.put("ALARM_CLOCK", Setting.getSidebar("ALARM_CLOCK"));
        //驾驶模式
        tagMap.put("VEHICLE_PLAYER", Setting.getSidebar("VEHICLE_PLAYER"));
        //音乐云盘
        tagMap.put("PRIVATE_CLOUD", Setting.getSidebar("PRIVATE_CLOUD"));
        //优惠券
        tagMap.put("DISCOUNT_COUPON", Setting.getSidebar("DISCOUNT_COUPON"));
        //6.0.0新增
        //游戏中心
        tagMap.put("GAME", Setting.getSidebar("GAME"));
        //彩铃专区
        tagMap.put("COLOR_RING", Setting.getSidebar("COLOR_RING"));
        //加入网易音乐人
        tagMap.put("MUSICIAN_VIEWER", Setting.getSidebar("MUSICIAN_VIEWER"));
        //演出
        tagMap.put("TICKET", Setting.getSidebar("TICKET"));
        //6.4.3新增
        //创作者中心
        tagMap.put("MUSICIAN_CREATOR_CENTER", Setting.getSidebar("MUSICIAN_CREATOR_CENTER"));
        //我的订单
        tagMap.put("MY_ORDER", Setting.getSidebar("MY_ORDER"));
        //青少年模式
        tagMap.put("YOUTH_MODE", Setting.getSidebar("YOUTH_MODE"));
        //7.0.0新增
        //音乐红包
        tagMap.put("RED_PACKET", Setting.getSidebar("RED_PACKET"));
        //新创作者中心
        tagMap.put("CREATOR_CENTER", Setting.getSidebar("MUSICIAN_CREATOR_CENTER"));

        tagMap.put("div2", Setting.getSidebar("div2"));
        tagMap.put("div3", Setting.getSidebar("div3"));
        tagMap.put("VIP_CARD", Setting.getSidebar("VIP_CARD"));

        XposedHelpers.findAndHookMethod(findClass(classMainDrawerString, context.getClassLoader()), methodRefreshDrawerString, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                removeUselessItem(param, versionCode);
            }
        });
    }

    private void removeUselessItem(XC_MethodHook.MethodHookParam param, int versionCode) {
        LinearLayout drawerContainer;
        LinearLayout dynamicContainer = null;
        drawerContainer = (LinearLayout) getObjectField(param.thisObject, objectMDrawerContainerString);
        if (versionCode >= 138)
            dynamicContainer = (LinearLayout) getObjectField(param.thisObject, "mDynamicContainer");
        removeItemInner(drawerContainer);
        removeItemInner(dynamicContainer);

        if (tagMap.get("div2")) {
            try {
                View div2 = (View) getObjectField(param.thisObject, "div2");
                div2.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }
        if (tagMap.get("div3")) {
            try {
                View div3 = (View) getObjectField(param.thisObject, "div3");
                div3.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }

        if (tagMap.get("VIP_CARD")) {
            try {
                View mMainActivityDrawerHeaderCard = (View) getObjectField(param.thisObject, "mMainActivityDrawerHeaderCard");
                mMainActivityDrawerHeaderCard.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }
    }

    private void removeItemInner(LinearLayout container) {
        if (container == null) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            Object tag = v.getTag();
            if (tag != null && shouldRemove(tag)) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private boolean shouldRemove(Object drawerItemEnum) {
        if (!drawerItemEnum.getClass().getName().equals(classDrawerItemEnumString))
            return false;
        String name = drawerItemEnum.toString();
        return tagMap.get(name) != null && tagMap.get(name);
    }
}
