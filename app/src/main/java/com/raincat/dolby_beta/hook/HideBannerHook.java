package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.raincat.dolby_beta.helper.SettingHelper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/23
 *     desc   : 移除Banner
 *     version: 1.0
 * </pre>
 */

public class HideBannerHook {
    public HideBannerHook(Context context) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_banner_hide_key))
            return;

        Class<?> mainBannerContainerClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.ui.MainBannerContainer", context.getClassLoader());
        if (mainBannerContainerClass == null)
            mainBannerContainerClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.ui.NeteaseMusicViewFlipper", context.getClassLoader());
        if (mainBannerContainerClass != null)
            XposedHelpers.findAndHookMethod(mainBannerContainerClass, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = 1;//改成0将导致无法下滑刷新
                    view.setLayoutParams(layoutParams);
                    view.setVisibility(View.GONE);
                }
            });

        Class<?> playlistBannerContainerClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.ui.PlaylistBanner", context.getClassLoader());
        if (playlistBannerContainerClass != null)
            XposedHelpers.findAndHookConstructor(playlistBannerContainerClass, Context.class, AttributeSet.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    final View view = (View) param.thisObject;
                    view.post(() -> {
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);
                        view.setVisibility(View.GONE);
                    });
                }
            });
    }
}
