package com.raincat.dolby_beta.view.sign;

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
 *     time   : 2021/09/17
 *     desc   : 打卡数量
 *     version: 1.0
 * </pre>
 */

public class SignCountView  extends BaseDialogInputItem {
    public SignCountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SignCountView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignCountView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.sign_count_title;
        editView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        setData(SettingHelper.getInstance().getSignCount() + "", SettingHelper.sign_count_default + "");

        defaultView.setOnClickListener(view -> {
            editView.setText(SettingHelper.sign_count_default + "");
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
                SettingHelper.getInstance().setSignCount(editView.getText().toString());
            }
        });
    }
}
