package com.raincat.dolby_beta.view.beauty;

import android.content.Context;
import android.util.AttributeSet;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.view.BaseDialogItem;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/10/22
 *     desc   : 侧边栏精简
 *     version: 1.0
 * </pre>
 */

public class BeautySidebarHideItem extends BaseDialogItem {
    public BeautySidebarHideItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BeautySidebarHideItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautySidebarHideItem(Context context) {
        super(context);
    }

    public void initData(LinkedHashMap<String, String> sidebarMap, HashMap<String, Boolean> sidebarSettingMap, String sidebarKey) {
        title = sidebarMap.get(sidebarKey) + "(" + sidebarKey + ")";
        key = sidebarKey;
        setData(true, sidebarSettingMap.get(sidebarKey));
        setOnClickListener(view -> {
            SettingHelper.getInstance().setSidebarSetting(key, !checkBox.isChecked());
            setData(true, sidebarSettingMap.get(sidebarKey));
        });
    }
}
