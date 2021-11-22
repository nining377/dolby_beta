package com.raincat.dolby_beta.hook;

import android.content.Context;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/05/30
 *     desc   : 评论区优先显示“最热”内容
 *     version: 1.0
 * </pre>
 */
public class CommentHotClickHook {
    public CommentHotClickHook(Context context) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.beauty_comment_hot_key))
            return;
        Class<?> commentDataClass = ClassHelper.CommentDataClass.getClazz();
        if (commentDataClass != null) {
            XposedBridge.hookAllConstructors(commentDataClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object object = param.thisObject;
                    Field[] fields = object.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && field.getType() == int.class) {
                            field.setAccessible(true);
                            Object o = field.get(object);
                            if ((int) o == 0)
                                field.set(object, 2);
                        }
                    }
                }
            });

            Class<?> sortTypeListClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.module.comment2.meta.SortTypeList", context.getClassLoader());
            if (sortTypeListClass == null)
                sortTypeListClass = XposedHelpers.findClassIfExists("com.netease.cloudmusic.music.biz.comment.meta.SortTypeList", context.getClassLoader());
            if (sortTypeListClass != null)
                XposedHelpers.findAndHookMethod(sortTypeListClass, "parseList", JSONArray.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        JSONArray array = (JSONArray) param.args[0];
                        JSONArray array2 = new JSONArray();
                        array2.put(array.getJSONObject(1));
                        array2.put(array.getJSONObject(2));
                        array2.put(array.getJSONObject(0));
                        param.args[0] = array2;
                    }
                });
        }
    }
}