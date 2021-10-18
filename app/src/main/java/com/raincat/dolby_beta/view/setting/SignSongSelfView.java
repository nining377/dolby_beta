package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.helper.SignSongHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/17
 *     desc   : 自助打卡
 *     version: 1.0
 * </pre>
 */

public class SignSongSelfView extends BaseDialogItem {
    public SignSongSelfView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SignSongSelfView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignSongSelfView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.sign_self_title;
        setData(false, false);

        setOnClickListener(view -> {
            SignSongHelper.showSelfSignDialog(context);
        });
    }
}
