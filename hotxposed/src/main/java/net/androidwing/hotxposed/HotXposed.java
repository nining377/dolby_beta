package net.androidwing.hotxposed;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created  on 2018/3/30.
 */
public class HotXposed {
    public static void hook(Class clazz, XC_LoadPackage.LoadPackageParam lpparam)
            throws Exception {
        String packageName = clazz.getName().replace("." + clazz.getSimpleName(), "");
        File apkFile = getApkFile(packageName, lpparam);

        if (!apkFile.exists()) {
            Log.e("error", "apk file not found");
            return;
        }

        filterNotify(lpparam);

        PathClassLoader classLoader = new PathClassLoader(apkFile.getAbsolutePath(), lpparam.getClass().getClassLoader());
        Class cls = classLoader.loadClass(clazz.getName());
        if (cls != null) {
            Method method = cls.getDeclaredMethod("dispatch", XC_LoadPackage.LoadPackageParam.class);
            method.setAccessible(true);
            method.invoke(cls.newInstance(), lpparam);
        }
    }

    private static void filterNotify(XC_LoadPackage.LoadPackageParam lpparam)
            throws ClassNotFoundException {
        if ("de.robv.android.xposed.installer".equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass("de.robv.android.xposed.installer.util.NotificationUtil"),
                    "showModulesUpdatedNotification", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new Object());
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }
                    });
        }
    }

    private static File getApkFile(String packageName, XC_LoadPackage.LoadPackageParam lpparam) throws PackageManager.NameNotFoundException {
        Context systemContext = (Context) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext");
        ApplicationInfo applicationInfo = systemContext.getPackageManager().getApplicationInfo(packageName, 0);
        return new File(applicationInfo.sourceDir);
    }
}
