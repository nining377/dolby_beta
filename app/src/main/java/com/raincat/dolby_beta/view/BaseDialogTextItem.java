package com.raincat.dolby_beta.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.raincat.dolby_beta.utils.Tools;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/15
 *     desc   : 控件
 *     version: 1.0
 * </pre>
 */

public class BaseDialogTextItem extends AppCompatTextView {
    public BaseDialogTextItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public BaseDialogTextItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseDialogTextItem(Context context) {
        super(context);
        init(context, null);
    }

    protected void init(Context context, AttributeSet attrs) {
        int padding = Tools.dp2px(context, 10);
        setPadding(padding, 8, padding, 0);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        setTextColor(Color.BLACK);
        setVisibility(GONE);
    }
}
