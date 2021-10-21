package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.ClassHelper;

import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

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

        Class<?> superClass = ClassHelper.MainActivitySuperClass.getClazz(context);
        Method[] setTabItemMethods = ClassHelper.MainActivitySuperClass.getTabItem();
        if (superClass != null && setTabItemMethods.length != 0) {
            for (Method method : setTabItemMethods) {
                findAndHookMethod(superClass, method.getName(), String[].class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String tabName = Arrays.toString((String[]) param.args[0]);
                        if ((tabName.contains("我的") && tabName.contains("发现")) || (tabName.contains("mine") && tabName.contains("main"))) {
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