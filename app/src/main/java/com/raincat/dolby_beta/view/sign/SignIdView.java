package com.raincat.dolby_beta.view.sign;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogInputItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/17
 *     desc   : 歌曲列表id
 *     version: 1.0
 * </pre>
 */

public class SignIdView extends BaseDialogInputItem {
    public SignIdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SignIdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignIdView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.sign_id_title;
        setData(SettingHelper.getInstance().getSignId(), "");

        defaultView.setText("清空");
        defaultView.setOnClickListener(view -> {
            editView.setText("");
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
                SettingHelper.getInstance().setSignId(editView.getText().toString());
            }
        });
    }
}
