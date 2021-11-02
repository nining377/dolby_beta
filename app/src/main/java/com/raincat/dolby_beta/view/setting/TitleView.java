package com.raincat.dolby_beta.view.setting;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.raincat.dolby_beta.BuildConfig;
import com.raincat.dolby_beta.view.BaseDialogItem;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/13
 *     desc   : 标题
 *     version: 1.0
 * </pre>
 */

public class TitleView extends BaseDialogItem {
    public TitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleView(Context context) {
        super(context);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextPaint paint = titleView.getPaint();
        paint.setFakeBoldText(true);

        title = "杜比大喇叭β v" + BuildConfig.VERSION_NAME;
        sub = "本模块仅供学习交流，严禁用于商业用途，请于24小时内删除。\n" +
                "注意：模块工作原理为音源替换而非破解，所以单曲付费与无版权歌曲有几率匹配错误，真心支持歌手请付费。";
        setData(false, false);
    }
}
