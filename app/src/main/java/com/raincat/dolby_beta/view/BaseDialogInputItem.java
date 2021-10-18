package com.raincat.dolby_beta.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raincat.dolby_beta.utils.Tools;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/09
 *     desc   : 控件
 *     version: 1.0
 * </pre>
 */

public class BaseDialogInputItem extends LinearLayout {
    private BaseDialogItem item;
    private Context context;

    protected TextView titleView, defaultView;
    protected EditText editView;

    protected String title, defaultText;

    public BaseDialogInputItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public BaseDialogInputItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseDialogInputItem(Context context) {
        super(context);
        init(context, null);
    }

    protected void init(Context context, AttributeSet attrs) {
        this.context = context;

        int padding = Tools.dp2px(context, 10);
        setPadding(padding, 10, padding, 10);
        setMinimumHeight(Tools.dp2px(context, 40));
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        addView(linearLayout);
        LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        linearLayout.setLayoutParams(layoutParams);

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        titleView.setTextColor(Color.BLACK);
        editView = new EditText(context);
        editView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editView.setTextColor(Color.BLACK);
        editView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(titleView);
        linearLayout.addView(editView);
        defaultView = new TextView(context);
        defaultView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        defaultView.setTextColor(Color.DKGRAY);
        defaultView.setText("恢复默认");
        addView(defaultView);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            titleView.setTextColor(Color.LTGRAY);
            editView.setTextColor(Color.LTGRAY);
            defaultView.setTextColor(Color.LTGRAY);
        } else {
            titleView.setTextColor(Color.BLACK);
            editView.setTextColor(Color.BLACK);
            defaultView.setTextColor(Color.DKGRAY);
        }
        defaultView.setEnabled(enabled);
        editView.setEnabled(enabled);
    }

    protected void setData(String text, String defaultText) {
        this.defaultText = defaultText;

        if (title != null && title.length() != 0)
            titleView.setText(title);

        if (TextUtils.isEmpty(text))
            editView.setText(defaultText);
        else
            editView.setText(text);
        editView.setSelection(editView.getText().length());
    }

    /**
     * 依附于某个item，当该item未勾选时，本item为不可选状态
     */
    public void setBaseOnView(BaseDialogItem item) {
        this.item = item;
        refresh();
    }

    public void refresh() {
        if (item != null) {
            setEnabled(item.getCheckBoxStatus());
        }
    }
}
