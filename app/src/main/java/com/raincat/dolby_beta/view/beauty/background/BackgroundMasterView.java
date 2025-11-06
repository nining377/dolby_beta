package com.raincat.dolby_beta.view.beauty.background;

import android.content.Context;
import android.util.AttributeSet;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;



public class BackgroundMasterView extends BaseDialogItem {
    public BackgroundMasterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BackgroundMasterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundMasterView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.beauty_background_title;
        key = SettingHelper.beauty_background_key;
        setData(true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}
