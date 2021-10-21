package com.raincat.dolby_beta.hook;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/10/24
 *     desc   : 测试
 *     version: 1.0
 * </pre>
 */

public class TestHook {
    public TestHook(Context context) {
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.netease.cloudmusic.v1.w.d", context.getClassLoader()), "t", List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                XposedBridge.log("测试：" + param.args[0].toString() + " " + param.args[0].getClass().getName());
                List<String> list = new ArrayList<>();
                list.add("main");
                list.add("mine");
                list.add("follow");
                param.args[0] = list;
            }
        });

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.netease.cloudmusic.v1.w.d", context.getClassLoader()), "d", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log("测试：" + param.getResult().toString() + " " + param.getResult().getClass().getName());
                List<String> list = new ArrayList<>();
                list.add("main");
                list.add("mine");
                param.setResult(list);
            }
        });
    }
}
