package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.hook.SettingHook;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 *     time   : 2024/01/04
 *     desc   : 重置模块
 *     version: 1.0
 * </pre>
 */

public class ResetModuleView extends BaseDialogItem {
    public ResetModuleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ResetModuleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResetModuleView(Context context) {
        super(context);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        title = "重置模块";
        sub = "模块出现问题可以尝试重置";
        setData( false, false);

        setOnClickListener(view -> {
            SettingHelper.getInstance().resetSetting();
            sendBroadcast(SettingHelper.refresh_setting);
            Toast.makeText(context, "重置完成，手动重启网易云生效", Toast.LENGTH_SHORT).show();
        });
    }
}

