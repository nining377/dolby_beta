package com.raincat.dolby_beta.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/09/08
 *     desc   : 工具类
 *     version: 1.0
 * </pre>
 */

public class Tools {
    public static final String neteaseTinkerPath = "data/data/" + CloudMusicPackage.PACKAGE_NAME + "/tinker";
    public static final String neteaseDbPath = "data/data/" + CloudMusicPackage.PACKAGE_NAME + "/databases/SignedSong_1.db";
    public static final String sdcardDbPath = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Cache/SignedSong_1.db";

    /**
     * 获取线程名称
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        throw new RuntimeException("can't get current process name");
    }

    /**
     * 获取签名
     */
    public static String getSign(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            android.content.pm.Signature[] signs = packageInfo.signatures;
            android.content.pm.Signature sign = signs[0];
            byte[] signature = sign.toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(signature);
            byte[] digest = md.digest();
            return toHexString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Toast
     */
    public static void showToastOnLooper(final Context context, final String message) {
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag;
        if (filePath == null || filePath.length() == 0)
            return false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        try {
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return false;
            }
            flag = true;
            File[] files = dirFile.listFiles();
            //遍历删除文件夹下的所有文件(包括子目录)
            for (File file : files) {
                if (file.isFile()) {
                    //删除子文件
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag) break;
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag) break;
                }
            }
            if (!flag) return false;
        } catch (Exception e) {
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    private static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 从SD卡中读取一个文件
     */
    public static List<String> readFileFromSD(String path) {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (!file.isDirectory()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    list.add(line);
                }
                inputStream.close();
            } catch (Exception e) {
                Log.d("Tools", e.getMessage());
            }
        }
        return list;
    }

    /**
     * 写入内容到一个文件
     */
    public static void writeFileFromSD(String path, List<String> content) {
        BufferedWriter out = null;
        try {
            File file = new File(path);
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "utf-8"));
            for (String s : content) {
                out.write(s);
                out.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件
     */
    public static void copyFile(String originalPath, String targetPath) {
        File originalFile = new File(originalPath);
        File targetFile = new File(targetPath);
        if (originalFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(originalFile);
                FileOutputStream fos = new FileOutputStream(targetFile);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件夹里所有文件
     */
    static void copyFiles(String originalPath, String targetPath) {
        try {
            File newFile = new File(targetPath);
            newFile.mkdirs();
            File oldFile = new File(originalPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (originalPath.endsWith(File.separator)) {
                    temp = new File(originalPath + file);
                } else {
                    temp = new File(originalPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFiles(originalPath + "/" + file, targetPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(targetPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压文件
     */
    public static boolean unZipFile(String zipFileString, String outPathString) {
        try {
            // 创建解压目标目录
            File outPath = new File(outPathString);
            // 如果目标目录不存在，则创建
            if (!outPath.exists()) {
                outPath.mkdirs();
            }

            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
            ZipEntry zipEntry;
            String fileName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                fileName = zipEntry.getName();
                if (zipEntry.isDirectory()) {//文件夹
                    fileName = fileName.substring(0, fileName.length() - 1);
                    File folder = new File(outPathString + File.separator + fileName);
                    folder.mkdirs();
                } else {//文件
                    File file = new File(outPathString + File.separator + fileName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            inZip.close();

            fileName = fileName.substring(0, fileName.indexOf(File.separator));
            copyFiles(outPathString + File.separator + fileName, outPathString);
            deleteDirectory(outPathString + File.separator + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断是否为64bit手机
     *
     * @return true是64位
     */
    public static boolean is64BitImpl() {
        try {
            Class<?> clzVMRuntime = Class.forName("dalvik.system.VMRuntime");
            if (clzVMRuntime == null) {
                return false;
            }
            Method mthVMRuntimeGet = clzVMRuntime.getDeclaredMethod("getRuntime");
            if (mthVMRuntimeGet == null) {
                return false;
            }
            Object objVMRuntime = mthVMRuntimeGet.invoke(null);
            if (objVMRuntime == null) {
                return false;
            }
            Method sVMRuntimeIs64BitMethod = clzVMRuntime.getDeclaredMethod("is64Bit");
            if (sVMRuntimeIs64BitMethod == null) {
                return false;
            }
            Object objIs64Bit = sVMRuntimeIs64BitMethod.invoke(objVMRuntime);
            if (objIs64Bit instanceof Boolean) {
                return (boolean) objIs64Bit;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    static List<String> filterList(List<String> list, Pattern pattern) {
        return Stream.of(list)
                .filter(s -> pattern.matcher(s).find())
                .toList();
    }

    private static String chinaIP;

    public static String getChinaIP() {
        if (chinaIP == null) {
//            Random rand = new Random();
            chinaIP = "111.63.128.23";
        }
        return chinaIP;
    }

    /**
     * ADB命令
     */
    public static void shell(Context context, Command command) {
        try {
            RootTools.closeAllShells();
            RootTools.getShell(false).add(command);
        } catch (TimeoutException | RootDeniedException | IOException e) {
            e.printStackTrace();
            showToastOnLooper(context, e.getMessage());
        }
    }
}