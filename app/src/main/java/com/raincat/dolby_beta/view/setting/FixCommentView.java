package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/11/30
 *     desc   : 评论区加载失败
 *     version: 1.0
 * </pre>
 */

public class FixCommentView extends BaseDialogItem {
    public FixCommentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixCommentView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = SettingHelper.fix_comment_title;
        sub = SettingHelper.fix_comment_sub;
        key = SettingHelper.fix_comment_key;
        setData(true, SettingHelper.getInstance().getSetting(key));

        setOnClickListener(view -> {
            SettingHelper.getInstance().setSetting(key, !checkBox.isChecked());
            sendBroadcast(SettingHelper.refresh_setting);
        });
    }
}
