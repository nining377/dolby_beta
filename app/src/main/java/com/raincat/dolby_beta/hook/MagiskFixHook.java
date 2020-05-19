package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Environment;

import com.annimon.stream.Stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MagiskFixHook {
    public MagiskFixHook(Context context) {
        Method[] methods = XposedHelpers.findMethodsByExactParameters(
                XposedHelpers.findClass("com.netease.cloudmusic.utils.NeteaseMusicUtils", context.getClassLoader()), List.class, boolean.class);
        Method method = Stream.of(methods).sortBy(Method::getName).findFirst().get();
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                List<String> list = new ArrayList<>();
                list.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                param.setResult(list);
            }
        });
    }
}





