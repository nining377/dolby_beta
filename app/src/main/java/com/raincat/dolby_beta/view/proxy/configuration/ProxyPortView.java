package com.raincat.dolby_beta.view.proxy.configuration;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogInputItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/09
 *     desc   : 代理端口
 *     version: 1.0
 * </pre>
 */

public class ProxyPortView extends BaseDialogInputItem {
    public ProxyPortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyPortView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyPortView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.proxy_port_title;
        editView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        setData(SettingHelper.getInstance().getProxyPort() + "", SettingHelper.proxy_port_default + "");

        defaultView.setOnClickListener(view -> {
            editView.setText(SettingHelper.proxy_port_default + "");
            editView.setSelection(editView.getText().length());
        });

        editView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SettingHelper.getInstance().setProxyPort(editView.getText().toString());
            }
        });
    }
}
