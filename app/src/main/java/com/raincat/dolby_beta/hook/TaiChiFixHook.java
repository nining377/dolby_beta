package com.raincat.dolby_beta.hook;

import com.raincat.dolby_beta.utils.CloudMusicPackage;

import org.json.JSONObject;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/01/09
 *     desc   : 修复太极优化后失效的bug
 *     version: 1.0
 * </pre>
 */
public class TaiChiFixHook {
    public TaiChiFixHook() {
        for (final Method m : CloudMusicPackage.HttpResult.getRawStringMethodList()) {
            XposedBridge.hookMethod(m, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.getResult() != null) {
                        if (param.getResult() instanceof String) {
                            String result = (String) param.getResult();
                            param.setResult(result);
                        }

                        if (param.getResult() instanceof JSONObject) {
                            JSONObject result = (JSONObject) param.getResult();
                            param.setResult(result);
                        }
                    }
                }
            });
        }
    }
}
