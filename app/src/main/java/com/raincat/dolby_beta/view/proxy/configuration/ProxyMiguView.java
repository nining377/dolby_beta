package com.raincat.dolby_beta.view.proxy.configuration;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogInputItem;

/**
 * <pre>
 *     author : Luoxingran
 *     e-mail : szb5845201314@gmail.com
 *     time   : 2023/07/24
 *     desc   : 咪咕cookie
 *     version: 1.0
 * </pre>
 */

public class ProxyMiguView extends BaseDialogInputItem {
    public ProxyMiguView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProxyMiguView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyMiguView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.migu_cookie_title;
       // editView.setKeyListener(DigitsKeyListener.getInstance("0123456789_.qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM; "));
        setData(SettingHelper.getInstance().getMiguCookie(), SettingHelper.migu_cookie_default);

        defaultView.setOnClickListener(view -> {
            editView.setText(SettingHelper.migu_cookie_default);
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
                SettingHelper.getInstance().setMiguCookie(editView.getText().toString());
            }
        });
    }
}
