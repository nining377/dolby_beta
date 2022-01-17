package com.raincat.dolby_beta.helper;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.raincat.dolby_beta.BuildConfig;
import com.raincat.dolby_beta.Hook;
import com.raincat.dolby_beta.net.HTTPSTrustManager;
import com.raincat.dolby_beta.utils.Tools;
import com.stericson.RootShell.execution.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/26
 *     desc   : 脚本帮助类
 *     version: 1.0
 * </pre>
 */

public class ScriptHelper {
    private static String scriptPath;
    public static String modulePath;

    private static Context neteaseContext;
    private static boolean loadSuccess = false;

    /**
     * 运行node，会阻塞线程
     */
    public static native int startNodeWithArguments(String[] arguments);

    /**
     * 设置node环境变量
     */
    public static native void setEnv(String name, String value);

    /**
     * 初始化脚本
     *
     * @param cover 是否覆盖
     */
    public static void initScript(Context context, boolean cover) {
        File unblockFile = new File(getScriptPath(context));
        if (cover || !unblockFile.exists() || !(BuildConfig.VERSION_CODE + "").equals(ExtraHelper.getExtraDate(ExtraHelper.APP_VERSION))) {
            if (FileHelper.unzipFile(modulePath, getScriptPath(context), "assets", "UnblockNeteaseMusic.zip")) {
                FileHelper.unzipFiles(getScriptPath(context) + "/UnblockNeteaseMusic.zip", getScriptPath(context));
            }
            String bit = context.getApplicationInfo().nativeLibraryDir.endsWith("64") ? "arm64-v8a" : "armeabi-v7a";
            FileHelper.unzipFile(modulePath, getScriptPath(context), bit, "libc++_shared.so");
            FileHelper.unzipFile(modulePath, getScriptPath(context), bit, "libnative-lib.so");
            FileHelper.unzipFile(modulePath, getScriptPath(context), bit, "libnode.so");
            FileHelper.unzipFile(modulePath, getScriptPath(context), "assets", "node");
            Command auth = new Command(0, "cd " + getScriptPath(context), "chmod 0777 *");
            Tools.shell(context, auth);
            ExtraHelper.setExtraDate(ExtraHelper.APP_VERSION, BuildConfig.VERSION_CODE);
        }
        initNative(context);
    }

    /**
     * 初始化NDK
     */
    public static void initNative(Context context) {
        if (loadSuccess)
            return;
        neteaseContext = context;
        String[] libName = new String[]{"c++_shared", "node", "native-lib"};
        try {
            System.loadLibrary(libName[0]);
            System.loadLibrary(libName[1]);
            System.loadLibrary(libName[2]);
            loadSuccess = true;
        } catch (UnsatisfiedLinkError e) {
            String nodePath = TextUtils.isEmpty(modulePath) ? "" : modulePath.substring(0, modulePath.lastIndexOf('/'));
            final String[] libPath = new String[]{
                    getScriptPath(context) + "/lib%s.so",
                    nodePath + "/lib/arm64/lib%s.so",
                    nodePath + "/lib/arm/lib%s.so",
                    modulePath + "!/lib/arm64-v8a/lib%s.so",
                    modulePath + "!/lib/armeabi-v7a/lib%s.so"
            };
            for (String lib : libPath) {
                try {
                    System.load(String.format(lib, libName[0]));
                    System.load(String.format(lib, libName[1]));
                    System.load(String.format(lib, libName[2]));
                    loadSuccess = true;
                    break;
                } catch (UnsatisfiedLinkError ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 采用兼容模式执行UnblockNeteaseMusic
     */
    public static void startScriptCompatibilityMode(Context context) {
        if (loadSuccess) {
            new Thread(() -> {
                setEnv("ENABLE_FLAC", SettingHelper.getInstance().getSetting(SettingHelper.proxy_flac_key) + "");
                setEnv("MIN_BR", SettingHelper.getInstance().getSetting(SettingHelper.proxy_priority_key) ? "256000" : "96000");
                setEnv("NODE_TLS_REJECT_UNAUTHORIZED", "0");

                String[] origin = SettingHelper.getInstance().getProxyOriginal().split(" ");
                ArrayList<String> scriptList = new ArrayList<>();
                scriptList.add("node");
                scriptList.add(getScriptPath(context) + "/precompiled/app.js");
                scriptList.add("-a");
                scriptList.add("127.0.0.1");
                scriptList.add("-p");
                scriptList.add(SettingHelper.getInstance().getProxyPort() + ":" + (SettingHelper.getInstance().getProxyPort() + 1));
                scriptList.add("-o");
                scriptList.addAll(Arrays.asList(origin));
                startNodeWithArguments(scriptList.toArray(new String[0]));
                ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
            }).start();
        }
    }
    /**
     * 采用代理模式执行UnblockNeteaseMusic
     */
    public static void starhttptproxyMode(final Context context) {
        if (loadSuccess) {
          /*
            String PINGserver = String.format("ping %s", SettingHelper.getInstance().gethttpProxy());

            Command start = new Command(0, PINGserver) {
                @Override
               public void commandOutput(int id, String line) {
                  if (line.contains("字节=")||line.contains("bytes=")) {
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "1");
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, "3");
                        Tools.showToastOnLooper(neteaseContext, "服务器代理运行成功");
                    } else{
                      Tools.showToastOnLooper(neteaseContext, "服务器代理运行失败");
                       }
                  }

            };
            //Tools.shell(context, start);*/
                     String STOP_PROXY = "killall -9 node >/dev/null 2>&1";
                     new Command(0, STOP_PROXY);
                     ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "1");
                     ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, "3");
                     Tools.showToastOnLooper(neteaseContext, "服务器代理运行成功");

        }

    };

    public static void startScript(final Context context) {
        if (loadSuccess) {
            String STOP_PROXY = "killall -9 node >/dev/null 2>&1";
            String START_PROXY = String.format("export ENABLE_FLAC=%s&&export MIN_BR=%s&&export NODE_TLS_REJECT_UNAUTHORIZED=0&&./node app.js -o %s -p %s",
                    SettingHelper.getInstance().getSetting(SettingHelper.proxy_flac_key), SettingHelper.getInstance().getSetting(SettingHelper.proxy_priority_key) ? "256000" : "96000",
                    SettingHelper.getInstance().getProxyOriginal(), SettingHelper.getInstance().getProxyPort() + ":" + (SettingHelper.getInstance().getProxyPort() + 1));

            Command start = new Command(0, STOP_PROXY, "cd " + getScriptPath(context), START_PROXY) {
                @Override
                public void commandOutput(int id, String line) {
                    if ((!line.contains("mERROR") && line.contains("Error:")) || line.contains("Port ") || line.contains("Please ")) {
                        Intent intent = new Intent(Hook.msg_send_notification);
                        intent.putExtra("message", line);
                        intent.putExtra("title", "脚本产生如下错误信息，若脚本因此无法运行请提issue");
                        neteaseContext.sendBroadcast(intent);
                    } else if (line.contains("HTTP Server running")) {
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "1");
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, "3");
                        Tools.showToastOnLooper(neteaseContext, "UnblockNeteaseMusic运行成功");
                    } else if (line.contains("Killed")) {
                        ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "0");
                        if (context != null) {
                            startScript(context);
                        }
                    } else if (line.contains("64-bit ELF file")) {
                        Intent intent = new Intent(Hook.msg_send_notification);
                        intent.putExtra("message", line);
                        intent.putExtra("title", "Node无法启动，请尝试兼容模式");
                        neteaseContext.sendBroadcast(intent);
                    }
                }
            };
            Tools.shell(context, start);
        }
    }

    /**
     * 打印代理日志
     *
     * @param level 日志级别
     */
    private static void getLogcatInfo(int level, byte[] tag, byte[] textByte) {
        if (level != 4 || textByte.length < 4)
            return;
        String text = new String(textByte, StandardCharsets.UTF_8);
        if (text.contains("lock"))
            return;
        if ((!text.contains("mERROR") && text.contains("Error:")) || text.contains("Port ") || text.contains("Please ")) {
            Intent intent = new Intent(Hook.msg_send_notification);
            intent.putExtra("message", text);
            intent.putExtra("title", "脚本产生如下错误信息，若脚本因此无法运行请提issue");
            neteaseContext.sendBroadcast(intent);
        } else if (text.contains("HTTP Server running")) {
            ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "1");
            ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_RETRY, "3");
            Tools.showToastOnLooper(neteaseContext, "UnblockNeteaseMusic以兼容模式运行成功");
        }
    }

    public static String getScriptPath(Context context) {
        if (TextUtils.isEmpty(scriptPath))
            scriptPath = context.getFilesDir().getAbsolutePath() + "/script";
        return scriptPath;
    }

    /**
     * 获取CA证书
     */
    public static SSLSocketFactory getSSLSocketFactory(Context context) {
        SSLContext sslContext = null;
        try {
            File ca = new File(getScriptPath(context) + File.separator + "ca.crt");
            if (ca.exists()) {
                InputStream certificate = new FileInputStream(ca);
                Certificate certificate1 = CertificateFactory.getInstance("X.509").generateCertificate(certificate);
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate1);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            } else {
                TrustManager[] trustManagers = new TrustManager[]{new HTTPSTrustManager()};
                try {
                    sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustManagers, new SecureRandom());
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sslContext != null)
            return sslContext.getSocketFactory();
        else
            return null;
    }
}
