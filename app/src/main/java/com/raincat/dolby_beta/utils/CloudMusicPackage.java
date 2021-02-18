package com.raincat.dolby_beta.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import com.annimon.stream.Stream;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 网易云动态hook
 *     version: 1.0
 * </pre>
 */

public class CloudMusicPackage {
    private static String codePath = "";
    public static int versionCode = 0;

    public final static String PACKAGE_NAME = "com.netease.cloudmusic";
    public final static String PACKAGE_SIGN = "DA:6B:06:9D:A1:E2:98:2D:B3:E3:86:23:3F:68:D7:6D";
    public final static String TAICHI_PACKAGE_SIGN = "04:B9:80:86:8C:AC:83:F6:BB:4F:B5:29:95:4C:C0:20";
    public final static String CACHE_PATH = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Ad";
    public final static String CACHE_PATH2 = Environment.getExternalStorageDirectory() + "/Android/data/com.netease.cloudmusic/cache/Ad";

    private static WeakReference<List<String>> allClassList = new WeakReference<>(null);

    public static void init(Context context) throws PackageManager.NameNotFoundException {
        NeteaseMusicApplication.init(context);
        codePath = context.getFilesDir().getAbsolutePath();
        versionCode = context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0).versionCode;
    }

    public static ClassLoader getClassLoader() {
        try {
            return NeteaseMusicApplication.getApplication().getClassLoader();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class ClassHelper {
        private static File getApkPath() throws PackageManager.NameNotFoundException, IllegalAccessException {
            ApplicationInfo applicationInfo = NeteaseMusicApplication.getApplication().getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
            return new File(applicationInfo.sourceDir);
        }

        static synchronized List<String> getAllClasses() {
            List<String> list = allClassList.get();
            if (list == null) {
                if (Setting.isDexEnabled())
                    list = Tools.readFileFromSD(codePath + File.separator + "class-" + versionCode);
                else
                    list = new ArrayList<>();

                if (list.size() == 0) {
                    try {
                        File apkFile = getApkPath();
                        // 不用 ZipDexContainer 因为会验证zip里面的文件是不是dex，会慢一点
                        Enumeration zip = new ZipFile(apkFile).entries();
                        while (zip.hasMoreElements()) {
                            ZipEntry dexInZip = (ZipEntry) zip.nextElement();
                            if (dexInZip.getName().startsWith("classes") && dexInZip.getName().endsWith(".dex")) {
                                MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry = DexFileFactory.loadDexEntry(apkFile, dexInZip.getName(), true, null);
                                DexBackedDexFile dexFile = dexEntry.getDexFile();

                                for (DexBackedClassDef classDef : dexFile.getClasses()) {
                                    String classType = classDef.getType();
                                    if (classType.contains("com/netease/cloudmusic") || classType.contains("okhttp3")) {
                                        classType = classType.substring(1, classType.length() - 1).replace("/", ".");
                                        list.add(classType);
                                    }
                                }
                            }
                        }

                        allClassList = new WeakReference<>(list);
                    } catch (Exception t) {
                        try {
                            File apkFile = getApkPath();
                            MultiDexContainer<? extends DexBackedDexFile> container = DexFileFactory.loadDexContainer(apkFile, null);
                            for (int i = 0; i < container.getDexEntryNames().size(); i++) {
                                MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry = container.getEntry(container.getDexEntryNames().get(i));
                                assert dexEntry != null;
                                DexBackedDexFile dexFile = dexEntry.getDexFile();

                                for (DexBackedClassDef classDef : dexFile.getClasses()) {
                                    String classType = classDef.getType();
                                    if (classType.contains("com/netease/cloudmusic")) {
                                        classType = classType.substring(1, classType.length() - 1).replace("/", ".");
                                        list.add(classType);
                                    }
                                }
                            }

                            allClassList = new WeakReference<>(list);
                        } catch (Exception e) {
                            //
                        }
                    }

                    Tools.writeFileFromSD(codePath + File.separator + "class-" + versionCode, list);
                }
            }
            return list;
        }

        static List<String> getFilteredClasses(Pattern pattern, Comparator<String> comparator) {
            List<String> list = Tools.filterList(getAllClasses(), pattern);
            Collections.sort(list, comparator);
            return list;
        }
    }

    public static class Transfer {
        private static Method checkMd5Method;
        private static Method checkDownloadStatusMethod;

        //下载完后的MD5检查
        public static Method getCheckMd5Method() {
            if (checkMd5Method == null) {
                Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.transfer\\.download\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    checkMd5Method = Stream.of(list)
                            .map(c -> findClass(c, getClassLoader()).getDeclaredMethods())
                            .flatMap(Stream::of)
                            .filter(m -> m.getParameterTypes().length == 4)
                            .filter(m -> m.getParameterTypes()[0] == File.class)
                            .filter(m -> m.getParameterTypes()[1] == File.class)
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("can't find checkMd5Method");
                }
            }
            return checkMd5Method;
        }

        //下载之前下载状态检查
        public static Method getCheckDownloadStatusMethod() {
            if (checkDownloadStatusMethod == null) {
                Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.transfer\\.download\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    checkDownloadStatusMethod = Stream.of(list)
                            .map(c -> findClass(c, getClassLoader()).getDeclaredMethods())
                            .flatMap(Stream::of)
                            .filter(m -> m.getReturnType() == long.class)
                            .filter(m -> m.getParameterTypes().length == 5)
                            .filter(m -> m.getParameterTypes()[1] == int.class)
                            .filter(m -> m.getParameterTypes()[3] == File.class)
                            .filter(m -> m.getParameterTypes()[4] == long.class)
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("can't find checkDownloadStatusMethod");
                }
            }
            return checkDownloadStatusMethod;
        }
    }

    public static class NeteaseMusicApplication {
        private static Class<?> clazz;
        private static Field singletonField;

        static void init(Context context) {
            clazz = findClass("com.netease.cloudmusic.NeteaseMusicApplication", context.getClassLoader());
            singletonField = XposedHelpers.findFirstFieldByExactType(getClazz(), getClazz());
        }

        static Class<?> getClazz() {
            return clazz;
        }

        static Application getApplication() throws IllegalAccessException {
            return (Application) singletonField.get(null);
        }
    }

    public static class HttpResult {
        private static final List<Method> rawStringMethodList = new ArrayList<>();
        private static Class<?> clazz;

        static Class<?> getClazz() {
            if (clazz == null) {
                if (versionCode == 138) {
                    clazz = findClass("com.netease.cloudmusic.j.g.d.a", CloudMusicPackage.getClassLoader());
                } else if (versionCode == 110) {
                    clazz = findClass("com.netease.cloudmusic.g.f.d.a", CloudMusicPackage.getClassLoader());
                } else if (versionCode >= 154) {
                    Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    if (list.isEmpty()) {
                        throw new RuntimeException("init failed");
                    }

                    clazz = Stream.of(list)
                            .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> Objects.requireNonNull(c.getSuperclass()).getName().contains("com.netease.cloudmusic.network"))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType().getName().endsWith("AntiSpamService")))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == boolean.class))
                            .findFirst()
                            .get();
                }
            }

            return clazz;
        }

        public static List<Method> getRawStringMethodList() {
            if (rawStringMethodList.isEmpty()) {
                List<Method> list = new ArrayList<>();
                list.addAll(Arrays.asList(findMethodsByExactParameters(getClazz(), JSONObject.class)));
                list.addAll(Arrays.asList(findMethodsByExactParameters(getClazz(), String.class)));

                rawStringMethodList.addAll(Stream.of(list)
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .filter(m -> !Modifier.isFinal(m.getModifiers()))
                        .filter(m -> !Modifier.isStatic(m.getModifiers()))
                        .toList());

            }
            return rawStringMethodList;
        }
    }

    public static class MainActivitySuperClass {
        private static Class<?> clazz;
        private static Method[] methods;

        public static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Class<?> mainActivityClass = findClass("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader());
                clazz = mainActivityClass.getSuperclass();
            }
            return clazz;
        }

        public static Method[] getTabItem() {
            if (methods == null && clazz != null) {
                methods = findMethodsByExactParameters(clazz, void.class, String[].class);
            }
            return methods;
        }
    }

    public static class OKHttp3Response {
        private static Class<?> clazz;

        final Object okHttp3Response;

        public OKHttp3Response(Object okHttp3Response) {
            this.okHttp3Response = okHttp3Response;
        }

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern = Pattern.compile("^okhttp3\\.[a-zA-Z]{1,8}$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> Modifier.isFinal(c.getModifiers()))
                        .filter(c -> c.getInterfaces().length == 1)
                        .filter(c -> c.getInterfaces()[0] == Closeable.class)
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == int.class))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == String.class))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == long.class))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        public Object getHeadersObject() throws IllegalAccessException {
            Field[] fields = getClazz().getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(f -> Stream.of(f.getType()).anyMatch(pf -> pf == OKHttp3Header.getClazz()))
                    .filter(f -> Stream.of(f.getType().getDeclaredFields()).anyMatch(pf -> pf.getType() == String[].class))
                    .findFirst().get();

            dataField.setAccessible(true);
            return dataField.get(okHttp3Response);
        }
    }

    public static class OKHttp3Header {
        private static Class<?> clazz;

        final Object okHttp3Header;

        public OKHttp3Header(Object okHttp3Header) {
            this.okHttp3Header = okHttp3Header;
        }

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern = Pattern.compile("^okhttp3\\.[a-zA-Z]{1,7}$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> Modifier.isFinal(c.getModifiers()))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == String[].class))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        public String[] getHeaders() throws IllegalAccessException {
            Field[] fields = getClazz().getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(f -> Stream.of(f.getType()).anyMatch(pf -> pf == String[].class))
                    .findFirst().get();

            dataField.setAccessible(true);
            return (String[]) dataField.get(okHttp3Header);
        }
    }

    public static class HttpResponse {
        private static Class<?> clazz;
        private static Method getResultMethod;

        final Object httpResponse;

        public HttpResponse(Object httpResponse) {
            this.httpResponse = httpResponse;
        }

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> Modifier.isFinal(c.getModifiers()))
                        .filter(c -> c.getSuperclass() == Object.class)
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == OKHttp3Response.getClazz()))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        public Object getResponseObject() throws IllegalAccessException {
            Field[] fields = getClazz().getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(f -> Stream.of(f.getType().getInterfaces()).anyMatch(i -> i == Closeable.class))
                    .filter(f -> Stream.of(f.getType().getDeclaredFields()).anyMatch(pf -> pf.getType().getName().startsWith("okhttp3")))
                    .findFirst().get();

            dataField.setAccessible(true);
            return dataField.get(httpResponse);
        }

        public Object getEapi() throws IllegalAccessException {
            Field[] fields = getClazz().getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(c -> Modifier.isAbstract(c.getType().getModifiers()))
                    .filter(c -> c.getType().getSuperclass() == Object.class)
                    .filter(c -> Stream.of(c.getType().getDeclaredFields()).anyMatch(m -> m.getType().getName().startsWith("okhttp3")))
                    .findFirst().get();

            dataField.setAccessible(true);
            return dataField.get(httpResponse);
        }

        public static Method getResultMethod() {
            if (getResultMethod == null) {
                List<Method> methodList = Arrays.asList(getClazz().getDeclaredMethods());

                getResultMethod = Stream.of(methodList)
                        .filter(m -> m.getExceptionTypes().length == 2)
                        .findFirst()
                        .get();
            }
            return getResultMethod;
        }
    }

    public static class HttpApi {
        private static Class<?> clazz;

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                        .filter(c -> Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> c.getSuperclass() == Object.class)
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType().getName().startsWith("okhttp3")))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        public static Uri getUri(Object eapi) throws IllegalAccessException {
            Field uriField = XposedHelpers.findFirstFieldByExactType(getClazz(), Uri.class);
            uriField.setAccessible(true);
            return (Uri) uriField.get(eapi);
        }
    }

    public static class HttpParams {
        private static Class<?> clazz;
        private static Field paramsMap;

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, CloudMusicPackage.getClassLoader()))
                        .filter(c -> Stream.of(c.getInterfaces()).anyMatch(i -> i == Serializable.class))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == LinkedHashMap.class))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        static Field getParamsMapField() {
            if (paramsMap == null) {
                Field[] fields = getClazz().getDeclaredFields();
                paramsMap = Stream.of(fields)
                        .filter(c -> Stream.of(c.getType()).anyMatch(m -> m == LinkedHashMap.class))
                        .findFirst().get();
                paramsMap.setAccessible(true);
            }
            return paramsMap;
        }

        public static LinkedHashMap<String, String> getParams(Object eapi) throws IllegalAccessException {
            List<Method> list = new ArrayList<>(Arrays.asList(findMethodsByExactParameters(eapi.getClass(), getClazz())));
            if (list != null && list.size() != 0) {
                Object params = XposedHelpers.callMethod(eapi, list.get(0).getName());
                LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) getParamsMapField().get(params);
                Uri uri = HttpApi.getUri(eapi);
                for (String name : uri.getQueryParameterNames()) {
                    String val = uri.getQueryParameter(name);
                    map.put(name, val != null ? val : "");
                }
                return (LinkedHashMap<String, String>) getParamsMapField().get(params);
            }
            return new LinkedHashMap<>();
        }
    }
}
