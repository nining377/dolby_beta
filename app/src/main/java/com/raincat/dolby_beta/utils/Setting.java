package com.raincat.dolby_beta.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.raincat.dolby_beta.BuildConfig;
import com.raincat.dolby_beta.R;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XSharedPreferences;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/09/08
 *     desc   : 配置
 *     version: 1.0
 * </pre>
 */

public class Setting {
    private static XSharedPreferences preferences;
    private static WeakReference<Resources> moduleResources = new WeakReference<>(null);

    private static XSharedPreferences getModuleSharedPreferences() {
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            preferences.makeWorldReadable();
        } else
            preferences.reload();
        return preferences;
    }

    private static Context getSystemContext() {
        Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
        return (Context) callMethod(activityThread, "getSystemContext");
    }

    private static String getModuleResString(int resId) {
        Resources resources = moduleResources.get();
        if (resources == null) {
            try {
                resources = getSystemContext().createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY).getResources();
                moduleResources = new WeakReference<>(resources);
            } catch (PackageManager.NameNotFoundException e) {
                return "";
            }
        }
        return resources.getString(resId);
    }

    public static boolean isEnable() {
        String valueString = getModuleResString(R.string.unblock_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.unblock_key), defaultValue);
    }

    public static boolean isProxyEnabled() {
        String valueString = getModuleResString(R.string.proxy_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.proxy_key), defaultValue);
    }

    public static boolean isGrayEnabled() {
        String valueString = getModuleResString(R.string.gray_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.gray_key), defaultValue);
    }

    public static boolean isHideTabEnabled() {
        String valueString = getModuleResString(R.string.hide_tab_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.hide_tab_key), defaultValue);
    }

    public static boolean isHideMainBannerEnabled() {
        String valueString = getModuleResString(R.string.hide_banner_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.hide_banner_key), defaultValue);
    }

    public static boolean isHidePlaylistBannerEnabled() {
        String valueString = getModuleResString(R.string.hide_playlist_banner_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.hide_playlist_banner_key), defaultValue);
    }

    public static boolean isHideBubbleEnabled() {
        String valueString = getModuleResString(R.string.hide_bubble_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.hide_bubble_key), defaultValue);
    }

    public static boolean isCommentHotEnabled() {
        String valueString = getModuleResString(R.string.comment_hot_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.comment_hot_key), defaultValue);
    }

    public static boolean isInternalEnabled() {
        String valueString = getModuleResString(R.string.internal_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.internal_key), defaultValue);
    }

    public static boolean isOverseaModeEnabled() {
        String valueString = getModuleResString(R.string.oversea_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.oversea_key), defaultValue);
    }

    public static boolean isMagiskEnabled() {
        String valueString = getModuleResString(R.string.magisk_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.magisk_key), defaultValue);
    }

    public static boolean isDexEnabled() {
        String valueString = getModuleResString(R.string.dex_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.dex_key), defaultValue);
    }

    public static boolean isBlackEnabled() {
        String valueString = getModuleResString(R.string.black_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.black_key), defaultValue);
    }

    public static boolean isUpdateEnabled() {
        String valueString = getModuleResString(R.string.remove_update_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.remove_update_key), defaultValue);
    }

    public static boolean isAutoSignInEnabled() {
        String valueString = getModuleResString(R.string.auto_sign_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.auto_sign_key), defaultValue);
    }

    public static boolean isSignSongEnabled() {
        String valueString = getModuleResString(R.string.sign_song_default_value);
        boolean defaultValue = Boolean.parseBoolean(valueString);

        return getModuleSharedPreferences().getBoolean(getModuleResString(R.string.sign_song_key), defaultValue);
    }

    public static boolean isCookieEnabled() {
        return getModuleSharedPreferences().getBoolean("cookie", false);
    }

    public static boolean getSidebar(String key) {
        return getModuleSharedPreferences().getBoolean(key, false);
    }

    public static int getLevel() {
        return getModuleSharedPreferences().getInt("level", -1);
    }

    public static int getFollows() {
        return getModuleSharedPreferences().getInt("follows", -1);
    }

    public static int getFans() {
        return getModuleSharedPreferences().getInt("fans", -1);
    }

    public static int getRole() {
        return getModuleSharedPreferences().getInt("role", -1);
    }

    public static String getNodeFile() {
        return getModuleSharedPreferences().getString("node", "node");
    }

    public static String getScriptFile() {
        return getModuleSharedPreferences().getString("script", "script.zip");
    }
}
