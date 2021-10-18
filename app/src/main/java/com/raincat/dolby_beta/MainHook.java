package com.raincat.dolby_beta;

import com.raincat.dolby_beta.helper.ScriptHelper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/10/23
 *     desc   : hook入口
 *     version: 1.0
 * </pre>
 */
public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private final static String PACKAGE_NAME = "com.netease.cloudmusic";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Exception {
        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            new Hook(lpparam);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        ScriptHelper.modulePath = startupParam.modulePath;
    }
}
