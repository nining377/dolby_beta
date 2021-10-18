package com.raincat.dolby_beta.hook;
import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/12/11
 *     desc   : 内测与听歌识曲弹窗
 *     version: 1.0
 * </pre>
 */

public class InternalDialogHook {
    public InternalDialogHook(Context context, int versionCode) {
        if (versionCode < 138)
            return;

        Class<?> materialDialogHelperClass = findClassIfExists("com.netease.cloudmusic.ui.MaterialDiloagCommon.MaterialDialogHelper", context.getClassLoader());
        if (materialDialogHelperClass != null) {
            XposedBridge.hookAllMethods(materialDialogHelperClass, "materialDialog", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args[0].getClass().getName().contains("MainActivity")||param.args[0].getClass().getName().contains("IdentifyActivity"))
                        param.setResult(null);
                }
            });
        }
    }
}