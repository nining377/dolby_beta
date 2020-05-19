package com.raincat.dolby_beta.hook;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/12/11
 *     desc   : 内测弹窗
 *     version: 1.0
 * </pre>
 */

public class InternalDialogHook {
    public InternalDialogHook(Context context, int versionCode) {
        if (versionCode < 138)
            return;

        hookAllMethods(findClass("com.netease.cloudmusic.ui.MaterialDiloagCommon.MaterialDialogHelper", context.getClassLoader()), "materialDialog", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args != null && param.args.length == 9 && param.args[6] instanceof Boolean && param.args[8] instanceof Boolean && param.args[0].getClass().getName().equals("com.netease.cloudmusic.activity.MainActivity")) {
                    param.setResult(null);
                }
            }
        });
    }
}
