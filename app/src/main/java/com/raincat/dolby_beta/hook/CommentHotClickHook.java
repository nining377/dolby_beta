package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/30
 *     desc   : 打开评论后优先显示最热评论
 *     version: 1.0
 * </pre>
 */
public class CommentHotClickHook {
    private boolean firstInit = true;

    public CommentHotClickHook(Context context) {
        Class<?> commentFragmentClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.module.comment2.fragment.CommentFragment", context.getClassLoader());
        Class<?> commentFragmentAClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.module.comment2.fragment.CommentFragment$a", context.getClassLoader());
        Class<?> viewHolderClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.module.comment2.fragment.CommentFragment$a$a", context.getClassLoader());
        if (commentFragmentAClass == null || viewHolderClass == null)
            return;
        XposedHelpers.findAndHookMethod(commentFragmentAClass, "a", viewHolderClass, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (firstInit && (int) param.args[1] == 1) {
                    Object viewHolder = param.args[0];
                    View view = (View) XposedHelpers.getObjectField(viewHolder, "itemView");
                    view.post(view::performClick);
                    firstInit = false;
                }
//                else if ((int) param.args[1] == 0) {
//                    param.setResult(null);
//                }
            }
        });

        XposedHelpers.findAndHookMethod(commentFragmentClass, "onDestroy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                firstInit = true;
            }
        });
    }
}
