package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/26
 *     desc   : 通知栏警告
 *     version: 1.0
 * </pre>
 */

public class WarnView extends BaseDialogItem {
    public WarnView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

    public WarnView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

    public WarnView(Context context) {
            super(context);
        }

        @Override
        public void init(Context context, AttributeSet attrs) {
            super.init(context, attrs);
            title = SettingHelper.warn_title;
            sub = SettingHelper.warn_sub;
            key = SettingHelper.warn_key;
            setData(true, SettingHelper.getInstance().getSetting(key));

            setOnClickListener(view -> {
                SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
                sendBroadcast(SettingHelper.refresh_setting);
            });
        }
}
