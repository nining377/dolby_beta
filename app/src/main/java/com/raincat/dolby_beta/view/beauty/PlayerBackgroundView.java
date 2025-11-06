package com.raincat.dolby_beta.view.beauty;

import android.content.Context;
import android.util.AttributeSet;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : luoxingran
 *     e-mail : szb5845201314@gmail.com
 *     time   : 2023/08/22
 *     desc   : 播放界面背景
 *     version: 1.0
 * </pre>
 */

public class PlayerBackgroundView extends BaseDialogItem {
    public PlayerBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PlayerBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerBackgroundView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.background_title;
        key = SettingHelper.background_key;
        setData(false, false);

        setOnClickListener(view -> {
            sendBroadcast(SettingHelper.background_setting);
        });
    }
}
