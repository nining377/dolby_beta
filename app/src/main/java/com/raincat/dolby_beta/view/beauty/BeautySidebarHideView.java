package com.raincat.dolby_beta.view.beauty;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/22
 *     desc   : 侧边栏精简
 *     version: 1.0
 * </pre>
 */

public class BeautySidebarHideView extends BaseDialogItem {
    public BeautySidebarHideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BeautySidebarHideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautySidebarHideView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.beauty_sidebar_hide_title;
        key = SettingHelper.beauty_sidebar_hide_key;
        sub = SettingHelper.beauty_sidebar_hide_sub;
        setData(false, false);

        setOnClickListener(view -> {
            sendBroadcast(SettingHelper.sidebar_setting);
        });
    }
}
