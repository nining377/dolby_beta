package com.raincat.dolby_beta;

import com.raincat.dolby_beta.ui.MainActivity;

import net.androidwing.hotxposed.IHookerDispatcher;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : hook入口
 *     version: 1.0
 * </pre>
 */

public class HookerDispatcherSelf implements IHookerDispatcher {
    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(MainActivity.class.getName(), lpparam.classLoader,
                "isModuleActive", XC_MethodReplacement.returnConstant(true));
    }
}
