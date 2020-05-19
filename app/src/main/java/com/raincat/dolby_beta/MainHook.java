package com.raincat.dolby_beta;

import com.raincat.dolby_beta.utils.CloudMusicPackage;

import net.androidwing.hotxposed.HotXposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : hook入口
 *     version: 1.0
 * </pre>
 */
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(CloudMusicPackage.PACKAGE_NAME)) {
            HotXposed.hook(HookerDispatcher.class, lpparam);
        } else if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            HotXposed.hook(HookerDispatcherSelf.class, lpparam);
        }
    }
}
