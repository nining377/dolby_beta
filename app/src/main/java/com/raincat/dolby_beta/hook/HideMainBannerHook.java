package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/23
 *     desc   : 隐藏首页banner
 *     version: 1.0
 * </pre>
 */
public class HideMainBannerHook {
    private static String mainBannerContainerClassString = "com.netease.cloudmusic.ui.MainBannerContainer";

    public HideMainBannerHook(Context context, final int versionCode) {
        if (versionCode < 138)
            mainBannerContainerClassString = "com.netease.cloudmusic.ui.NeteaseMusicViewFlipper";

        findAndHookMethod(mainBannerContainerClassString, context.getClassLoader(), "onAttachedToWindow", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = 1;//改成0将导致无法下滑刷新
                view.setLayoutParams(layoutParams);
                view.setVisibility(View.GONE);
            }
        });
    }
}
