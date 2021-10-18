package com.raincat.dolby_beta.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.utils.Tools;


/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/11
 *     desc   : 控件
 *     version: 1.0
 * </pre>
 */

public class BaseDialogItem extends LinearLayout {
    private BaseDialogItem item;
    private Context context;

    protected CheckBox checkBox;
    protected TextView titleView, subView;

    protected String title, sub, key;

    public BaseDialogItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public BaseDialogItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseDialogItem(Context context) {
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
        subView = new TextView(context);
        subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        subView.setTextColor(Color.DKGRAY);
        linearLayout.addView(titleView);
        linearLayout.addView(subView);
        checkBox = new CheckBox(context);
        checkBox.setClickable(false);
        addView(checkBox);

        titleView.setVisibility(GONE);
        subView.setVisibility(GONE);
        checkBox.setVisibility(GONE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            titleView.setTextColor(Color.LTGRAY);
            subView.setTextColor(Color.LTGRAY);
        } else {
            titleView.setTextColor(Color.BLACK);
            subView.setTextColor(Color.DKGRAY);
        }
        checkBox.setEnabled(enabled);
    }

    protected void setData(boolean showCheck, boolean check) {
        if (title != null && title.length() != 0) {
            titleView.setText(title);
            titleView.setVisibility(VISIBLE);
        }
        if (sub != null && sub.length() != 0) {
            subView.setText(sub);
            subView.setVisibility(VISIBLE);
        }
        if (showCheck) {
            checkBox.setChecked(check);
            checkBox.setVisibility(VISIBLE);
        }
    }

    /**
     * 依附于某个item，当该item未勾选时，本item为不可选状态
     */
    public void setBaseOnView(BaseDialogItem item) {
        this.item = item;
        refresh();
    }

    protected boolean getCheckBoxStatus() {
        return checkBox.isChecked();
    }

    public void refresh() {
        if (item != null) {
            setEnabled(item.getCheckBoxStatus());
        }
        if (checkBox.getVisibility() == VISIBLE)
            checkBox.setChecked(SettingHelper.getInstance().getSetting(key));
    }

    protected void sendBroadcast(String action) {
        context.sendBroadcast(new Intent(action));
    }
}
