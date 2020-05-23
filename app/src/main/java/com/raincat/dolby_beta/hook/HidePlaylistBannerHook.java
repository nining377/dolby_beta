package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/23
 *     desc   : 隐藏歌单banner
 *     version: 1.0
 * </pre>
 */
public class HidePlaylistBannerHook {
    public HidePlaylistBannerHook(Context context, final int versionCode) {
        if (versionCode < 138)
            return;

        findAndHookConstructor("com.netease.cloudmusic.ui.PlaylistBanner", context.getClassLoader(), Context.class, AttributeSet.class, new XC_MethodHook() {
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
