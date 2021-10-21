package com.raincat.dolby_beta.helper;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/16
 *     desc   : 文件操作帮助
 *     version: 1.0
 * </pre>
 */

public class FileHelper {
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
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.getAbsolutePath());
                }
                if (!flag) break;
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
    static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 从SD卡中读取一个文件
     */
    static List<String> readFileFromSD(String path) {
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
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 写入内容到一个文件
     */
    static void writeFileFromSD(String path, List<String> content) {
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
     * 从assets中拷贝文件
     */
    public static void copyFilesAssets(AssetManager assetManager, String oldPath, String codePath) {
        try {
            String[] fileNames = assetManager.list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(codePath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesAssets(assetManager, oldPath + File.separator + fileName, codePath + File.separator + fileName);
                }
            } else {//如果是文件
                InputStream is = assetManager.open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(codePath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压一个文件
     */
    public static boolean unzipFile(String zipFileString, String outPathString, String fileParentName, String fileName) {
        try {
            File outPath = new File(outPathString);
            if (!outPath.exists()) {
                outPath.mkdirs();
            }

            ZipFile zipFile = new ZipFile(zipFileString);
            InputStream is;
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            ZipEntry entry;
            while (e.hasMoreElements()) {
                entry = e.nextElement();
                if (entry.getName().contains(fileParentName) && entry.getName().contains(fileName) && !entry.isDirectory()) {
                    is = zipFile.getInputStream(entry);
                    File dstFile = new File(outPathString + "/" + fileName);
                    FileOutputStream fos = new FileOutputStream(dstFile);
                    int len;
                    byte[] buffer = new byte[8192];
                    while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 解压整个zip
     */
    public static boolean unzipFiles(String zipFileString, String outPathString) {
        try {
            File outPath = new File(outPathString);
            if (!outPath.exists()) {
                outPath.mkdirs();
            }

            ZipFile zipFile = new ZipFile(zipFileString);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            ZipEntry entry;
            String szName = "";
            while (e.hasMoreElements()) {
                entry = e.nextElement();
                if (entry.isDirectory()) {
                    szName = entry.getName();
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    InputStream is = zipFile.getInputStream(entry);
                    File dstFile = new File(outPathString + "/" + entry.getName());
                    FileOutputStream fos = new FileOutputStream(dstFile);
                    int len;
                    byte[] buffer = new byte[8192];
                    while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
