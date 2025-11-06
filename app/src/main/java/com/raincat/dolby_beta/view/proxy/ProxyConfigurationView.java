package com.raincat.dolby_beta.view.proxy;

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

public class ProxyConfigurationView extends BaseDialogItem {
    public ProxyConfigurationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyConfigurationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyConfigurationView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.proxy_configuration_title;
        key = SettingHelper.proxy_configuration_key;
        sub = SettingHelper.proxy_configuration_sub;
        setData(false, false);

        setOnClickListener(view -> {
            sendBroadcast(SettingHelper.proxy_configuration_setting);
        });
    }
}
