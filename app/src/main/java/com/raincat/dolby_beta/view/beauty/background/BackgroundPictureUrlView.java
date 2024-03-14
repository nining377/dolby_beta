package com.raincat.dolby_beta.view.beauty.background;

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
 *     time   : 2023/08/22
 *     desc   : 图片URL
 *     version: 1.0
 * </pre>
 */

public class BackgroundPictureUrlView extends BaseDialogInputItem {
    public BackgroundPictureUrlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BackgroundPictureUrlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundPictureUrlView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.background_url_title;
        setData(SettingHelper.getInstance().getPictureUrl(), SettingHelper.background_url_default);

        defaultView.setOnClickListener(view -> {
            editView.setText(SettingHelper.background_url_default);
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
                SettingHelper.getInstance().setPictureUrl(editView.getText().toString());
            }
        });
    }
}
