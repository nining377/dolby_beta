package com.raincat.dolby_beta.view.proxy;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.ScriptHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/09
 *     desc   : 代理总开关
 *     version: 1.0
 * </pre>
 */

public class ProxyMasterView extends BaseDialogItem {
    public ProxyMasterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyMasterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyMasterView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.proxy_master_title;
        key = SettingHelper.proxy_master_key;
        setData(true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            ScriptHelper.initScript(context, false);
            ScriptHelper.startScript();
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}
