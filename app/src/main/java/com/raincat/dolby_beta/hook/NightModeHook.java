package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Pair;

import com.raincat.dolby_beta.helper.SettingHelper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/12/03
 *     desc   : 夜间模式
 *     version: 1.0
 * </pre>
 */

public class NightModeHook {
    private String resourceRouterInstanceMethodString = "getInstance";
    private String resourceRouterIsNightThemeMethodString = "isNightTheme";
    private String themeAgentInstanceMethodString = "getInstance";
    private String themeAgentSwitchTheme = "switchTheme";

    public NightModeHook(Context context, int versionCode) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_night_mode_key))
            return;
        Class<?> superActivityClass = findClass("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader());
        while (superActivityClass != null && !superActivityClass.getName().contains("AppCompatActivity"))
            superActivityClass = superActivityClass.getSuperclass();

        String resourceRouterClassString = "com.netease.cloudmusic.theme.core.ResourceRouter";
        String themeAgentClassString = "com.netease.cloudmusic.theme.core.ThemeAgent";
        String themeConfigClassString = "com.netease.cloudmusic.theme.core.ThemeConfig";
        String themeInfoClassString = "com.netease.cloudmusic.theme.core.ThemeInfo";

        if (versionCode == 110) {
            resourceRouterClassString = "com.netease.cloudmusic.theme.core.b";
            themeAgentClassString = "com.netease.cloudmusic.theme.core.c";
            themeConfigClassString = "com.netease.cloudmusic.theme.core.f";
            resourceRouterInstanceMethodString = "a";
            resourceRouterIsNightThemeMethodString = "d";
            themeAgentInstanceMethodString = "a";
            themeAgentSwitchTheme = "a";
        }

        final Class<?> resourceRouterClass = XposedHelpers.findClassIfExists(resourceRouterClassString, context.getClassLoader());
        final Class<?> themeAgentClass = XposedHelpers.findClassIfExists(themeAgentClassString, context.getClassLoader());
        final Class<?> themeConfigClass = XposedHelpers.findClassIfExists(themeConfigClassString, context.getClassLoader());
        final Class<?> themeInfoClass = XposedHelpers.findClassIfExists(themeInfoClassString, context.getClassLoader());

        XposedHelpers.findAndHookMethod(superActivityClass, "onStart", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context c = (Context) param.thisObject;
                Object resourceRouter = XposedHelpers.callStaticMethod(resourceRouterClass, resourceRouterInstanceMethodString);
                boolean isNight = (boolean) XposedHelpers.callMethod(resourceRouter, resourceRouterIsNightThemeMethodString);
                int nightModeFlags = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && !isNight) {
                    Object themeAgent = XposedHelpers.callStaticMethod(themeAgentClass, themeAgentInstanceMethodString);
                    Object themeInfo = XposedHelpers.newInstance(themeInfoClass, -3);
                    XposedHelpers.callMethod(themeAgent, themeAgentSwitchTheme, c, themeInfo, true);
                } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && isNight) {
                    Object themeAgent = XposedHelpers.callStaticMethod(themeAgentClass, themeAgentInstanceMethodString);
                    if (versionCode == 110) {
                        int prevThemeInfo = (int) XposedHelpers.callStaticMethod(themeConfigClass, "m");
                        Object themeInfo = XposedHelpers.newInstance(themeInfoClass, prevThemeInfo);
                        XposedHelpers.callMethod(themeAgent, themeAgentSwitchTheme, c, themeInfo, true);
                    } else {
                        Pair<Integer, Boolean> prevThemeInfo = (Pair<Integer, Boolean>) XposedHelpers.callStaticMethod(themeConfigClass, "getPrevThemeInfo");
                        Object themeInfo = XposedHelpers.newInstance(themeInfoClass, prevThemeInfo.first);
                        XposedHelpers.callMethod(themeAgent, themeAgentSwitchTheme, c, themeInfo, true);
                    }
                }
            }
        });
    }
}
