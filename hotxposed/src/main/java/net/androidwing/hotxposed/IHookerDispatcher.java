package net.androidwing.hotxposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created  on 2018/3/30.
 */
public interface IHookerDispatcher {
    void dispatch(XC_LoadPackage.LoadPackageParam loadPackageParam);
}
