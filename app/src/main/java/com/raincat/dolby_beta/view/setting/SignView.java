package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/13
 *     desc   : 签到
 *     version: 1.0
 * </pre>
 */

public class SignView extends BaseDialogItem {
    public SignView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SignView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.sign_title;
        key = SettingHelper.sign_key;
        sub = "启用可能会被警告说你在刷云贝";
        setData( true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}

