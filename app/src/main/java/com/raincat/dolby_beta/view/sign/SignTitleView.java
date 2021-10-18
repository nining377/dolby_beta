package com.raincat.dolby_beta.view.sign;

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
 *     time   : 2021/09/15
 *     desc   : 每日歌曲打卡
 *     version: 1.0
 * </pre>
 */

public class SignTitleView extends BaseDialogItem {
    public SignTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SignTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignTitleView(Context context) {
        super(context);
    }

    public void setTitle(String title) {
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextPaint paint = titleView.getPaint();
        paint.setFakeBoldText(true);

        this.title = title;
        setData(false, false);
    }
}
