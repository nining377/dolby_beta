package com.raincat.dolby_beta.view.proxy.configuration;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : Luoxingran
 *     e-mail : szb5845201314@gmail.com
 *     time   : 2023/07/24
 *     desc   : 代理配置标题
 *     version: 1.0
 * </pre>
 */

public class ProxyConfigurationTitleView extends BaseDialogItem {
    public ProxyConfigurationTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyConfigurationTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyConfigurationTitleView(Context context) {
        super(context);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextPaint paint = titleView.getPaint();
        paint.setFakeBoldText(true);

        title = SettingHelper.proxy_configuration_title;
        setData(false, false);
    }
}
