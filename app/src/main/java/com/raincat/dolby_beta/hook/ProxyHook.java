package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;


import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.ScriptHelper;
import com.raincat.dolby_beta.helper.SettingHelper;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

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

    private String fieldSSLSocketFactory;
    private String fieldHttpUrl = "url";
    private String fieldProxy = "proxy";
    private final List<String> whiteUrlList = Arrays.asList("song/enhance/player/url", "song/enhance/download/url","/package");

    public ProxyHook(Context context, boolean isPlayProcess) {
        Class<?> realCallClass = findClassIfExists("okhttp3.internal.connection.RealCall", context.getClassLoader());
        if (realCallClass != null) {
            fieldSSLSocketFactory = "sslSocketFactoryOrNull";
        } else {
            realCallClass = findClassIfExists("okhttp3.RealCall", context.getClassLoader());
            if (realCallClass != null)
                fieldSSLSocketFactory = "sslSocketFactory";
            else {
                realCallClass = findClassIfExists("okhttp3.z", context.getClassLoader());
                fieldSSLSocketFactory = "o";
                fieldHttpUrl = "a";
                fieldProxy = "d";
            }
        }

        hookAllConstructors(realCallClass, new XC_MethodHook() {
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

        Class<?> okHttpClientBuilderClass = XposedHelpers.findClassIfExists("okhttp3.OkHttpClient$Builder", context.getClassLoader());
        if (okHttpClientBuilderClass != null) {
            XposedBridge.hookAllMethods(okHttpClientBuilderClass, "addInterceptor", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args[0].getClass().getName().contains("com.netease.cloudmusic.network.cronet"))
                        param.setResult(param.thisObject);
//                        XposedBridge.hookAllMethods(param.args[0].getClass(), "intercept", new XC_MethodHook() {
//                            @Override
//                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                                super.beforeHookedMethod(param);
//                                Object object = param.args[0];
//                                if (object != null && object.getClass().getName().contains("Chain")) {
//                                    Object request = XposedHelpers.callMethod(object, "request");
//                                    if (request.toString().contains("song/enhance/player/url") || request.toString().contains("song/enhance/download/url")) {
//                                        Object response = XposedHelpers.callMethod(object, "proceed", request);
//                                        param.setResult(response);
//                                    }
//                                }
//                            }
//                        });
                }
            });
        }

        if (!isPlayProcess) {
            ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
            if (SettingHelper.getInstance().getSetting(SettingHelper.proxy_master_key)) {
                ScriptHelper.initScript(context, false);
                if (SettingHelper.getInstance().getSetting(SettingHelper.proxy_server_key)) {
                    ScriptHelper.startHttpProxyMode(context);
                } else {
                    ScriptHelper.startScript();
                }
            }
        }
    }

    /**
     * 设置代理
     */
    private void setProxy(Context context, Object client) throws Exception {
        //保存正常的代理与SSL
        Field sslSocketFactoryField = client.getClass().getDeclaredField(fieldSSLSocketFactory);
        sslSocketFactoryField.setAccessible(true);
        Field proxyField = client.getClass().getDeclaredField(fieldProxy);
        proxyField.setAccessible(true);
        if (objectProxy == null)
            objectProxy = proxyField.get(client);
        if (objectSSLSocketFactory == null)
            objectSSLSocketFactory = sslSocketFactoryField.get(client);

        if (ExtraHelper.getExtraDate(ExtraHelper.SCRIPT_STATUS).equals("1")) {
            String httpUrlHost = SettingHelper.getInstance().getSetting(SettingHelper.proxy_server_key) ?
                    SettingHelper.getInstance().getHttpProxy() : "127.0.0.1";
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpUrlHost, SettingHelper.getInstance().getProxyPort()));
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
