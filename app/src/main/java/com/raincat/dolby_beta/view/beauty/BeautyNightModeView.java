package com.raincat.dolby_beta.view.beauty;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/12/03
 *     desc   : 夜间模式
 *     version: 1.0
 * </pre>
 */

public class BeautyNightModeView extends BaseDialogItem {
    public BeautyNightModeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BeautyNightModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautyNightModeView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.beauty_night_mode_title;
        sub = SettingHelper.beauty_night_mode_sub;
        key = SettingHelper.beauty_night_mode_key;
        setData(true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}
