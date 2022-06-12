package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.content.Intent;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/12/21
 *     desc   : 隐藏头部hook
 *     version: 1.0
 * </pre>
 */

public class HideTabHook {
    public HideTabHook(Context context, int versionCode) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_tab_hide_key) || versionCode < 138)
            return;

        List<Method> setTabItemMethods = ClassHelper.MainActivitySuperClass.getTabItemStringMethods(context);
        if (setTabItemMethods != null && setTabItemMethods.size() != 0) {
            for (Method method : setTabItemMethods) {
                hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) {
                        if (param.args[0] == null || ((String[]) param.args[0]).length < 2)
                            return;
                        String[] tabNames = (String[]) param.args[0];
                        String tabName = Arrays.toString(tabNames);
                        if ((tabName.contains("我的") && tabName.contains("发现")) || (tabName.contains("mine") && tabName.contains("main"))) {
                            String[] strings = new String[2];
                            System.arraycopy(tabNames, 0, strings, 0, 2);
                            param.args[0] = strings;
                        }
                    }
                });
            }

            hookMethod(ClassHelper.MainActivitySuperClass.getViewPagerInitMethod(context), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Intent intent = (Intent) param.args[0];
                    intent.putExtra("SELECT_PAGE_INDEX", 0);
                }
            });
        }

        if (versionCode >= 8000010) {
            Class<?> bottomTabViewClass = ClassHelper.BottomTabView.getClazz(context);
            if (bottomTabViewClass != null) {
                findAndHookMethod(bottomTabViewClass, ClassHelper.BottomTabView.getTabInitMethod(context).getName(), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        List<String> list = new ArrayList<>();
                        list.add("mine");
                        list.add("main");
                        list.add("follow");
                        param.setResult(list);
                    }
                });

                findAndHookMethod(bottomTabViewClass, ClassHelper.BottomTabView.getTabRefreshMethod(context).getName(), List.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        List<String> list = new ArrayList<>();
                        list.add("mine");
                        list.add("main");
                        list.add("follow");
                        param.args[0] = list;
                    }
                });
            }
        }
    }
}