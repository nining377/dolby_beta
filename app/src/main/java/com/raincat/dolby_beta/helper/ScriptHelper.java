package com.raincat.dolby_beta.helper;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.raincat.dolby_beta.Hook;
import com.raincat.dolby_beta.net.HTTPSTrustManager;
import com.raincat.dolby_beta.utils.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
        if (cover || !unblockFile.exists())
            if (FileHelper.unzipFile(modulePath, getScriptPath(context), "UnblockNeteaseMusic.zip")) {
                FileHelper.unzipFiles(getScriptPath(context) + "/UnblockNeteaseMusic.zip", getScriptPath(context));
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
            String nodePath = modulePath.substring(0, modulePath.lastIndexOf('/'));
            final String[] libPath = new String[]{
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
                    if (ex.toString().contains("32-bit")) {
                        int neteaseBit = context.getApplicationInfo().nativeLibraryDir.endsWith("arm64") ? 64 : 32;
                        Tools.showToastOnLooper(context, "node加载失败，请安装" + neteaseBit + "位的大喇叭！");
                    }
                }
            }
        }
    }

    /**
     * 开始执行UnblockNeteaseMusic
     */
    public static void startScript(Context context) {
        if (loadSuccess) {
            new Thread(() -> {
                setEnv("ENABLE_FLAC", SettingHelper.getInstance().getSetting(SettingHelper.proxy_flac_key) + "");
                setEnv("MIN_BR", "128000");

                String[] origin = SettingHelper.getInstance().getProxyOriginal().split(" ");
                ArrayList<String> scriptList = new ArrayList<>();
                scriptList.add("node");
                scriptList.add(getScriptPath(context) + "/app.js");
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
     * 打印代理日志
     *
     * @param level 日志级别
     */
    private static void getLogcatInfo(int level, String tag, String text) {
        if (level != 4 || text.contains("lock"))
            return;
        if (text.contains("Error:") || text.contains("Port ") || text.contains("Please ")) {
            Intent intent = new Intent(Hook.msg_send_notification);
            intent.putExtra("content", text);
            neteaseContext.sendBroadcast(intent);
        } else if (text.contains("HTTP Server running")) {
            ExtraHelper.setExtraDate(ExtraHelper.SCRIPT_STATUS, "1");
            Tools.showToastOnLooper(neteaseContext, "UnblockNeteaseMusic运行成功");
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
