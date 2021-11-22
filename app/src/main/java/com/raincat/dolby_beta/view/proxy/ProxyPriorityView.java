package com.raincat.dolby_beta.view.proxy;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/11/12
 *     desc   : 音质优先
 *     version: 1.0
 * </pre>
 */

public class ProxyPriorityView  extends BaseDialogItem {
    public ProxyPriorityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyPriorityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyPriorityView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.proxy_priority_title;
        sub = SettingHelper.proxy_priority_sub;
        key = SettingHelper.proxy_priority_key;
        setData(true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}
