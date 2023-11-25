package com.raincat.dolby_beta;

import net.androidwing.hotxposed.IHookerDispatcher;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2019/10/23
 *     desc   : Hook入口
 *     version: 1.0
 * </pre>
 */

public class HookerDispatcher implements IHookerDispatcher {
    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam lpparam) {
        new Hook(lpparam);
        new HookOther(lpparam);
    }
}



