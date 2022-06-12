package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.model.SidebarEnum;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/22
 *     desc   : 侧边栏精简
 *     version: 1.0
 * </pre>
 */
public class HideSidebarHook {
    private Class<?> classDrawerItemEnum;
    private HashMap<String, Boolean> sidebarSettingMap = new HashMap<>();

    private String classMainDrawerString = "com.netease.cloudmusic.ui.MainDrawer";
    private String classDrawerItemEnumString = "com.netease.cloudmusic.ui.MainDrawer$DrawerItemEnum";
    private String methodRefreshDrawerString = "refreshDrawer";
    private String objectMDrawerContainerString = "mDrawerContainer";

    public HideSidebarHook(Context context, int versionCode) {
        if (versionCode < 138) {
            classMainDrawerString = "com.netease.cloudmusic.ui.l";
            classDrawerItemEnumString = "com.netease.cloudmusic.ui.l$b";
            methodRefreshDrawerString = "m";
            objectMDrawerContainerString = "i";
        }

        classDrawerItemEnum = XposedHelpers.findClassIfExists(classDrawerItemEnumString, context.getClassLoader());
        if (classDrawerItemEnum == null)
            classDrawerItemEnum = XposedHelpers.findClassIfExists("com.netease.cloudmusic.music.biz.sidebar.ui.MainDrawer$DrawerItemEnum", context.getClassLoader());
        if (classDrawerItemEnum != null && classDrawerItemEnum.isEnum()) {
            Object[] enumConstants = classDrawerItemEnum.getEnumConstants();
            SidebarEnum.setSidebarEnum(enumConstants);
            sidebarSettingMap = SettingHelper.getInstance().getSidebarSetting(SidebarEnum.getSidebarEnum());
        }

        if (versionCode >= 7003010 && ClassHelper.SidebarItem.getClazz(context) != null) {
            XposedBridge.hookAllConstructors(ClassHelper.SidebarItem.getClazz(context), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args.length == 2 && param.args[1] instanceof List) {
                        List<Object> objectList = (List<Object>) param.args[1];
                        for (Iterator<Object> iterator = objectList.iterator(); iterator.hasNext(); ) {
                            try {
                                Object object = iterator.next();
                                String enumString = XposedHelpers.callMethod(object, "getEnumType").toString();
                                if (!TextUtils.isEmpty(enumString) && !enumString.equals("SETTING")) {
                                    if (enumString.equals("GROUP")) {
                                        int group = (int) XposedHelpers.callMethod(object, "getGroup");
                                        if (group == 1 && sidebarSettingMap.get("GROUP1") != null || sidebarSettingMap.get("GROUP1")) {
                                            iterator.remove();
                                        } else if (group == 2 && sidebarSettingMap.get("GROUP2") != null || sidebarSettingMap.get("GROUP2")) {
                                            iterator.remove();
                                        }
                                    } else if (sidebarSettingMap.get(enumString) != null && sidebarSettingMap.get(enumString))
                                        iterator.remove();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else if (versionCode < 7003010) {
            Class<?> mainDrawerClass = XposedHelpers.findClassIfExists(classMainDrawerString, context.getClassLoader());
            if (mainDrawerClass != null)
                XposedHelpers.findAndHookMethod(mainDrawerClass, methodRefreshDrawerString, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        removeUselessItem(param, versionCode);
                    }
                });
        }
    }

    private void removeUselessItem(XC_MethodHook.MethodHookParam param, int versionCode) {
        LinearLayout drawerContainer;
        LinearLayout dynamicContainer = null;
        drawerContainer = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, objectMDrawerContainerString);
        if (versionCode >= 138)
            dynamicContainer = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mDynamicContainer");
        removeItemInner(drawerContainer);
        removeItemInner(dynamicContainer);

        if (sidebarSettingMap.get("DIV2") != null && sidebarSettingMap.get("DIV2")) {
            try {
                View div2 = (View) XposedHelpers.getObjectField(param.thisObject, "div2");
                div2.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }
        if (sidebarSettingMap.get("DIV3") != null && sidebarSettingMap.get("DIV3")) {
            try {
                View div3 = (View) XposedHelpers.getObjectField(param.thisObject, "div3");
                div3.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }
        if (sidebarSettingMap.get("DIV4") != null && sidebarSettingMap.get("DIV4")) {
            try {
                View div3 = (View) XposedHelpers.getObjectField(param.thisObject, "div4");
                div3.setVisibility(View.GONE);
            } catch (NoSuchFieldError ignored) {
            }
        }

        if (sidebarSettingMap.get("VIP") != null && sidebarSettingMap.get("VIP")) {
            try {
                View mMainActivityDrawerHeaderCard = (View) XposedHelpers.getObjectField(param.thisObject, "mMainActivityDrawerHeaderCard");
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
        if (drawerItemEnum.getClass().getName().equals("com.netease.cloudmusic.ui.MainDrawer$DrawerItemEnum")
                || drawerItemEnum.getClass().getName().equals("com.netease.cloudmusic.ui.l$b")) {
            String name = drawerItemEnum.toString();
            return sidebarSettingMap.get(name) != null && sidebarSettingMap.get(name);
        } else
            return false;
    }
}
