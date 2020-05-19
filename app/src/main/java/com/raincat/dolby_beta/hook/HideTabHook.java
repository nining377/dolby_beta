package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.utils.CloudMusicPackage;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;

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
        if (versionCode < 138)
            return;

        Class superClass = CloudMusicPackage.MainActivitySuperClass.getClazz(context);
        Method[] setTabItemMethods = CloudMusicPackage.MainActivitySuperClass.getTabItem();
        if (superClass != null && setTabItemMethods.length != 0) {
            for (Method method : setTabItemMethods) {
                findAndHookMethod(superClass, method.getName(), String[].class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String[] tabName = (String[]) param.args[0];
                        if (tabName.length == 4 && (tabName[0].equals("我的") || tabName[0].equals("Mine"))) {
                            String[] strings = new String[2];
                            System.arraycopy(tabName, 0, strings, 0, 2);
                            param.args[0] = strings;
                        }
                    }
                });
            }
        }
    }
}