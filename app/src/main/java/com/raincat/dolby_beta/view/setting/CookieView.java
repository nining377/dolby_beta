package com.raincat.dolby_beta.view.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/15
 *     desc   : 获取cookie
 *     version: 1.0
 * </pre>
 */

public class CookieView extends BaseDialogItem {
    public CookieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CookieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CookieView(Context context) {
        super(context);
    }

    @Override
    public void init(final Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.cookie_title;
        setData(false, false);

        setOnClickListener(view -> {
            //获取cookie
            String cookie = ExtraHelper.getExtraDate(ExtraHelper.COOKIE);
            if (cookie.equals("-1") || cookie.length() == 0) {
                Toast.makeText(context, "获取失败，请重新登录以获取cookie", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, cookie);
            Toast.makeText(context, "cookie已复制到剪切板", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clipData);
        });
    }
}
