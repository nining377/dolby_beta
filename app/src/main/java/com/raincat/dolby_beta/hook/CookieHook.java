package com.raincat.dolby_beta.hook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.utils.Setting;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class CookieHook {
    public CookieHook(Context context) {
        findAndHookMethod("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader(),
                "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        //获取cookie
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String cookie = ExtraDao.getInstance(context).getExtra("cookie");
                        if (cookie.equals("-1") || cookie.length() == 0) {
                            Toast.makeText(context, "获取失败，请重新登录以获取cookie", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ClipData clipData = ClipData.newPlainText(null, ExtraDao.getInstance(context).getExtra("cookie"));
                        Toast.makeText(context, "cookie已复制到剪切板", Toast.LENGTH_SHORT).show();
                        clipboard.setPrimaryClip(clipData);
                    }
                });
    }
}
