package com.raincat.dolby_beta.hook;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.raincat.dolby_beta.db.ExtraDao;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;
import com.stericson.RootShell.execution.Command;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     org    : Shenzhen JingYu Network Technology Co., Ltd.
 *     e-mail : nining377@gmail.com
 *     time   : 2021/03/10
 *     desc   : 音乐代理hook
 *     version: 1.0
 * </pre>
 */

public class UnblockMusicHook {
    private final static String STOP_PROXY = "killall -9 node >/dev/null 2>&1";
    private final static String START_PROXY = "./node app.js -o kuwo qq migu kugou -p 23338";

    private static String dataPath;

    private final String classMainActivity = "com.netease.cloudmusic.activity.MainActivity";
    private String classRealCall ;
    private String fieldHttpUrl = "url";
    private String fieldProxy = "proxy";

    public UnblockMusicHook(Context context, int versionCode, boolean isPlayProcess) {
        if (versionCode >= 7001080) {
            classRealCall = "okhttp3.internal.connection.RealCall";
        } else if (versionCode >= 138) {
            classRealCall = "okhttp3.RealCall";
        } else {
            classRealCall = "okhttp3.z";
            fieldHttpUrl = "a";
            fieldProxy = "d";
        }

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
                    if (urlObj.toString().contains("song/enhance/player/url") || urlObj.toString().contains("song/enhance/download/url")
                            || urlObj.toString().contains("eapi/privilege") || urlObj.toString().contains("eapi/album/privilege")) {
                        if (ExtraDao.getInstance(context).getExtra("ScriptRunning").equals("0")) {
                            Tools.showToastOnLooper(context, "node未运行，请保证脚本与Node文件路径正确！");
                        } else
                            proxyField.set(client, proxy);
                    } else {
                        proxyField.set(client, null);
                    }
                }
            }
        });

        if (isPlayProcess)
            return;

        findAndHookMethod(classMainActivity, context.getClassLoader(), "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ExtraDao.getInstance(context).saveExtra("ScriptRunning", "0");
                initScript(context);
            }
        });

        findAndHookMethod(classMainActivity, context.getClassLoader(), "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Command stop = new Command(0, STOP_PROXY);
                Tools.shell(context, stop);
                dataPath = null;
            }
        });
    }

    private void initScript(final Context c) {
        if (dataPath != null)
            return;

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
            dataPath = c.getFilesDir().getAbsolutePath() + File.separator + "script";
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

            Command start = new Command(0, STOP_PROXY, "cd " + dataPath, START_PROXY) {
                @Override
                public void commandOutput(int id, String line) {
                    if (line.contains("Error")) {
                        Tools.showToastOnLooper(c, "运行失败，错误为：" + line);
                    } else if (line.contains("HTTP Server running")) {
                        Tools.showToastOnLooper(c, "UnblockNeteaseMusic运行成功");
                        ExtraDao.getInstance(context).saveExtra("ScriptRunning", "1");
                    }
                }
            };
            Tools.shell(c, start);
        }
    }

    /**
     * 改变音质
     */
    private void quality() {
        List<String> scriptList = Tools.readFileFromSD(dataPath + File.separator + "src" + File.separator + "provider" + File.separator + "select.js");
        for (int i = 0; i < scriptList.size(); i++) {
            if (scriptList.get(i).contains("ENABLE_FLAC")) {
                if (Setting.isQualityEnabled())
                    scriptList.set(i, "module.exports.ENABLE_FLAC = 'true'");
                else
                    scriptList.set(i, "module.exports.ENABLE_FLAC = (process.env.ENABLE_FLAC || '').toLowerCase() === 'true'");
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
                    scriptList.set(i, "\t\tif ((item.code != 200 || item.freeTrialInfo || item.br <= 128000) && (target == 0 || item.id == target)) {");
                else
                    scriptList.set(i, "\t\tif ((item.code != 200 || item.freeTrialInfo) && (target == 0 || item.id == target)) {");
                break;
            }
        }
        Tools.writeFileFromSD(dataPath + File.separator + "src" + File.separator + "hook.js", scriptList);
    }
}
