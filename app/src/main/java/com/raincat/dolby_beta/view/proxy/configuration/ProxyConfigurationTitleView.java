package com.raincat.dolby_beta.view.proxy.configuration;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/10
 *     desc   : 代理标题
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
