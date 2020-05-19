package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.utils.Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 破除海外限制
 *     version: 1.0
 * </pre>
 */

public class OverseaHook {
    private static String requestBuilderClassString = "okhttp3.Request$Builder";
    private static String requestBuilderMethodString = "build";
    private static String headerBuilderFieldString = "headers";
    private static String headerBuilderListFieldString = "namesAndValues";
    private static String httpUrlFieldString = "url";

    public OverseaHook(Context context, int versionCode) {
        if (versionCode == 110) {
            requestBuilderClassString = "okhttp3.aa$a";
            requestBuilderMethodString = "b";
            headerBuilderFieldString = "c";
            headerBuilderListFieldString = "a";
            httpUrlFieldString = "a";
        }
        hookAllMethods(findClass(requestBuilderClassString, context.getClassLoader()), requestBuilderMethodString, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object request = param.thisObject;
                Field httpUrlField = request.getClass().getDeclaredField(httpUrlFieldString);
                httpUrlField.setAccessible(true);

                if (httpUrlField.get(request).toString().contains("music.163.com")||httpUrlField.get(request).toString().contains("music.126.net")) {
                    Field headerBuilderField = request.getClass().getDeclaredField(headerBuilderFieldString);
                    headerBuilderField.setAccessible(true);
                    Object headerBuilder = headerBuilderField.get(request);
                    Field headerBuilderListField = headerBuilder.getClass().getDeclaredField(headerBuilderListFieldString);
                    headerBuilderListField.setAccessible(true);
                    List<String> headerBuilderList = (List<String>) headerBuilderListField.get(headerBuilder);
                    if (headerBuilderList == null)
                        headerBuilderList = new ArrayList<>();
                    headerBuilderList.add("X-Real-IP");
                    headerBuilderList.add(Tools.getChinaIP());
                    headerBuilderListField.set(headerBuilder, headerBuilderList);
                }
            }
        });
    }
}
