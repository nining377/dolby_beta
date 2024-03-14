package com.raincat.dolby_beta;

import android.text.TextUtils;

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
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Exception {
        if (!TextUtils.isEmpty(lpparam.packageName) && lpparam.packageName.equals("com.netease.cloudmusic")) {
//            HotXposed.hook(HookerDispatcher.class, lpparam);
                new Hook(lpparam);
        }else if (!TextUtils.isEmpty(lpparam.packageName) && lpparam.packageName.equals("com.netease.cloudmusic.lite")) {
            new HookOther(lpparam);
        }else if (!TextUtils.isEmpty(lpparam.packageName) && lpparam.packageName.equals("com.hihonor.cloudmusic")) {
            new HookOther(lpparam);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        ScriptHelper.modulePath = startupParam.modulePath;
    }
}
