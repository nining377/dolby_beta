package com.raincat.dolby_beta.view.beauty.background;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;



public class BackgroundTitleView extends BaseDialogItem {
    public BackgroundTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BackgroundTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundTitleView(Context context) {
        super(context);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextPaint paint = titleView.getPaint();
        paint.setFakeBoldText(true);

        title = SettingHelper.background_title;
        setData(false, false);
    }
}
