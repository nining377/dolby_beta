package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.ScriptHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.utils.Tools;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/08
 *     desc   : 代理
 *     version: 1.0
 * </pre>
 */

public class ProxyHook {
    private static SSLSocketFactory socketFactory;
    private static Object objectProxy;
    private static Object objectSSLSocketFactory;

    private String classRealCall;
    private String fieldSSLSocketFactory;
    private String fieldHttpUrl = "url";
    private String fieldProxy = "proxy";

    private final List<String> whiteUrlList = Arrays.asList("song/enhance/player/url", "song/enhance/download/url");

    public ProxyHook(Context context, int versionCode, boolean isPlayProcess) {
        if (!SettingHelper.getInstance().isEnable(SettingHelper.proxy_master_key)) {
            ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
            return;
        }

        if (versionCode >= 7001080) {
            classRealCall = "okhttp3.internal.connection.RealCall";
            fieldSSLSocketFactory = "sslSocketFactoryOrNull";
        } else if (versionCode >= 114) {
            classRealCall = "okhttp3.RealCall";
            fieldSSLSocketFactory = "sslSocketFactory";
        } else {
            classRealCall = "okhttp3.z";
            fieldSSLSocketFactory = "o";
            fieldHttpUrl = "a";
            fieldProxy = "d";
        }

        hookAllConstructors(findClass(classRealCall, context.getClassLoader()), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length == 3) {
                    Object client = param.args[0];
                    Object request = param.args[1];

                    Field urlField = request.getClass().getDeclaredField(fieldHttpUrl);
                    urlField.setAccessible(true);

                    Object urlObj = urlField.get(request);

                    for (String url : whiteUrlList) {
                        if (urlObj.toString().contains(url)) {
                            setProxy(context, client);
                            break;
                        }
                    }
                }
            }
        });

        if (isPlayProcess)
            findAndHookMethod("com.netease.cloudmusic.service.PlayService", context.getClassLoader(), "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int retry = Integer.parseInt(ExtraHelper.getExtraDate(ExtraHelper.SCRIPT_RETRY));
                    if (retry > 0) {
                        ScriptHelper.initScript(context, retry == 1);
                        ScriptHelper.startScript(context);
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, --retry);
                    } else
                        Tools.showToastOnLooper(context, "重试次数过多，UnblockNeteaseMusic运行失败！");
                }
            });

        if (!isPlayProcess)
            findAndHookMethod("com.netease.cloudmusic.activity.LoadingActivity", context.getClassLoader(), "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
                    ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, "3");
                }
            });
    }

    /**
     * 设置代理
     */
    private void setProxy(Context context, Object client) throws Exception {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", SettingHelper.getInstance().getProxyPort()));
        Field proxyField = client.getClass().getDeclaredField(fieldProxy);
        proxyField.setAccessible(true);
        Field sslSocketFactoryField = client.getClass().getDeclaredField(fieldSSLSocketFactory);
        sslSocketFactoryField.setAccessible(true);
        if (objectProxy == null)
            objectProxy = proxyField.get(client);
        if (objectSSLSocketFactory == null)
            objectSSLSocketFactory = sslSocketFactoryField.get(client);

        if (ExtraHelper.getExtraDate(ExtraHelper.SCRIPT_STATUS).equals("1")) {
            proxyField.set(client, proxy);
            if (socketFactory == null)
                socketFactory = ScriptHelper.getSSLSocketFactory(context);
            if (socketFactory != null)
                sslSocketFactoryField.set(client, socketFactory);
        } else {
            proxyField.set(client, objectProxy);
            sslSocketFactoryField.set(client, objectSSLSocketFactory);
        }
    }
}
