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
 *     desc   : 音源代理
 *     version: 1.0
 * </pre>
 */

public class ProxyView extends BaseDialogItem {
    public ProxyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.proxy_title;
        key = SettingHelper.proxy_key;
        setData(false, false);

        setOnClickListener(view -> {
            sendBroadcast(SettingHelper.proxy_setting);
        });
    }
}
