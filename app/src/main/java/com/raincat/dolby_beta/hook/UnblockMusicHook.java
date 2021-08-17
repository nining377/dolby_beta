package com.raincat.dolby_beta.hook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;
import com.stericson.RootShell.execution.Command;

import java.io.File;
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
 *     time   : 2021/03/10
 *     desc   : 音乐代理hook
 *     version: 1.0
 * </pre>
 */

public class UnblockMusicHook {
    private final static String STOP_PROXY = "killall -9 node >/dev/null 2>&1";
    private final static String START_PROXY = "./node app.js -o kuwo qq migu kugou -a 127.0.0.1 -p 23338:23339";

    private static String dataPath;
    private static SSLSocketFactory socketFactory;
    private static Object objectProxy;
    private static Object objectSSLSocketFactory;

    private final String classMainActivity = "com.netease.cloudmusic.activity.MainActivity";
    private String classRealCall;
    private String fieldHttpUrl = "url";
    private String fieldProxy = "proxy";
    private String fieldSSLSocketFactory;

    private final List<String> whiteUrlList = Arrays.asList(
            "song/enhance/player/url", "song/enhance/download/url");

    private final List<String> blackUrlList = Arrays.asList("eapi/playlist/subscribe",
            "163yun.com");
    
    public UnblockMusicHook(Context context, int versionCode, boolean isPlayProcess) {
        if (versionCode >= 7001080) {
            classRealCall = "okhttp3.internal.connection.RealCall";
            fieldSSLSocketFactory = "sslSocketFactoryOrNull";
        } else if (versionCode >= 138) {
            classRealCall = "okhttp3.RealCall";
            fieldSSLSocketFactory = "sslSocketFactory";
        } else {
            classRealCall = "okhttp3.z";
            fieldSSLSocketFactory = "o";
            fieldHttpUrl = "a";
            fieldProxy = "d";
        }

        dataPath = context.getFilesDir().getAbsolutePath() + File.separator + "script";
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 23338));
        hookAllConstructors(findClass(classRealCall, context.getClassLoader()), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length == 3) {
                    Object client = param.args[0];
                    Object request = param.args[1];

                    Field urlField = request.getClass().getDeclaredField(fieldHttpUrl);
                    urlField.setAccessible(true);
                    Field proxyField = client.getClass().getDeclaredField(fieldProxy);
                    proxyField.setAccessible(true);

                    Object urlObj = urlField.get(request);

                    for (String url : blackUrlList) {
                        if (urlObj.toString().contains(url)) {
                            return;
                        }
                    }

                    if (Setting.isWhiteEnabled()) {
                        for (String url : whiteUrlList) {
                            if (urlObj.toString().contains(url)) {
                                if (ExtraDao.getInstance(context).getExtra("ScriptRunning").equals("1"))
                                    proxyField.set(client, proxy);
                                else
                                    Tools.showToastOnLooper(context, "node未成功运行，请到模块内选择正确的脚本与Node路径，若已使用存储重定向等APP请保证网易云音乐也可访问到脚本路径！");
                                break;
                            }
                        }
                    } else {
                        Field sslSocketFactoryField = client.getClass().getDeclaredField(fieldSSLSocketFactory);
                        sslSocketFactoryField.setAccessible(true);
                        if (objectProxy == null)
                            objectProxy = proxyField.get(client);
                        if (objectSSLSocketFactory == null)
                            objectSSLSocketFactory = sslSocketFactoryField.get(client);

                        if (ExtraDao.getInstance(context).getExtra("ScriptRunning").equals("0")) {
                            proxyField.set(client, objectProxy);
                            sslSocketFactoryField.set(client, objectSSLSocketFactory);
                        } else {
                            if (socketFactory == null)
                                socketFactory = Tools.getSLLContext(dataPath + File.separator + "ca.crt").getSocketFactory();
                            proxyField.set(client, proxy);
                            sslSocketFactoryField.set(client, socketFactory);
                        }
                    }
                }
            }
        });

        if (isPlayProcess)
            return;

        findAndHookMethod(classMainActivity, context.getClassLoader(), "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                final Context neteaseContext = (Context) param.thisObject;
                initScript(neteaseContext);
            }
        });

        findAndHookMethod(classMainActivity, context.getClassLoader(), "onDestroy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Command stop = new Command(0, STOP_PROXY);
                Tools.shell(context, stop);
            }
        });
    }

    private void initScript(final Context c) {
        long scriptLastUpdateTime = Long.parseLong(ExtraDao.getInstance(c).getExtra("script_time"));
        long nodeSize = Long.parseLong(ExtraDao.getInstance(c).getExtra("node_size"));
        String scriptFilePath = Setting.getScriptFile(), nodeFilePath = Setting.getNodeFile();
        File scriptFile = new File(scriptFilePath), nodeFile = new File(nodeFilePath);
        if (!scriptFilePath.endsWith("zip"))
            Tools.showToastOnLooper(c, "脚本文件请勿解压！");
        else if (!scriptFile.exists() && scriptLastUpdateTime < 0)
            Tools.showToastOnLooper(c, "脚本文件不存在！");
        else if (!nodeFile.exists())
            Tools.showToastOnLooper(c, "node文件不存在！");
        else {
            File dataFile = new File(dataPath);
            if (!dataFile.exists())
                dataFile.mkdirs();
            if (scriptFile.exists()) {
                if (scriptFile.lastModified() != scriptLastUpdateTime) {
                    if (Tools.unZipFile(scriptFilePath, dataPath))
                        ExtraDao.getInstance(c).saveExtra("script_time", scriptFile.lastModified() + "");
                }
                quality();
                force();
            }
            if (nodeFile.exists() && nodeSize != nodeFile.length()) {
                Tools.copyFile(nodeFilePath, dataPath + File.separator + "node");
                ExtraDao.getInstance(c).saveExtra("node_size", nodeFile.length() + "");
                Command auth = new Command(0, "cd " + dataPath, "chmod 770 *");
                Tools.shell(c, auth);
            }

            startScrip(c);
        }
    }

    private void startScrip(final Context c) {
        Command start = new Command(0, STOP_PROXY, "cd " + dataPath, START_PROXY) {
            @Override
            public void commandOutput(int id, String line) {
                if (line.contains("Error")) {
                    ExtraDao.getInstance(context).saveExtra("ScriptRunning", "0");
                    Tools.showToastOnLooper(c, "运行失败，错误为：" + line);
                } else if (line.contains("HTTP Server running")) {
                    ExtraDao.getInstance(context).saveExtra("ScriptRunning", "1");
                    Tools.showToastOnLooper(c, "UnblockNeteaseMusic运行成功");
                } else if (line.contains("Killed")) {
                    ExtraDao.getInstance(context).saveExtra("ScriptRunning", "0");
                    if (!((Activity) c).isFinishing()) {
                        Tools.showToastOnLooper(c, "Node被Killed，可能手机运存已耗尽，正在尝试重启……");
                        startScrip(c);
                    }
                }
            }
        };
        Tools.shell(c, start);
    }

    /**
     * 改变音质
     */
    private void quality() {
        List<String> scriptList = Tools.readFileFromSD(dataPath + File.separator + "src" + File.separator + "provider" + File.separator + "select.js");
        for (int i = 0; i < scriptList.size(); i++) {
            if (scriptList.get(i).contains("env.ENABLE_FLAC")) {
                if (Setting.isQualityEnabled())
                    scriptList.set(i, "(process.env.ENABLE_FLAC || 'true').toLowerCase() === 'true';");
                else
                    scriptList.set(i, "(process.env.ENABLE_FLAC || '').toLowerCase() === 'true';");
                break;
            }
        }
        Tools.writeFileFromSD(dataPath + File.separator + "src" + File.separator + "provider" + File.separator + "select.js", scriptList);
    }

    /**
     * 低音质强制代理
     */
    private void force() {
        List<String> scriptList = Tools.readFileFromSD(dataPath + File.separator + "src" + File.separator + "hook.js");
        for (int i = 0; i < scriptList.size(); i++) {
            if (scriptList.get(i).contains("item.code") && scriptList.get(i).contains("item.freeTrialInfo")) {
                if (Setting.isForceEnabled())
                    scriptList.set(i, "\t\t(item.code != 200 || item.freeTrialInfo || item.br <= 128000) &&");
                else
                    scriptList.set(i, "\t\t(item.code != 200 || item.freeTrialInfo) &&");
                break;
            }
        }
        Tools.writeFileFromSD(dataPath + File.separator + "src" + File.separator + "hook.js", scriptList);
    }
}
