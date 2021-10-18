package com.raincat.dolby_beta.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.annimon.stream.Stream;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/04/14
 *     desc   : 类加载帮助
 *     version: 1.0
 * </pre>
 */

public class ClassHelper {
    //类加载器
    private static ClassLoader classLoader = null;
    //dex缓存
    private static List<String> classCacheList = null;
    //dex缓存路径
    private static String classCachePath = null;
    //网易云版本
    private static int versionCode = 0;

    public static synchronized void getCacheClassList(final Context context, final int version, final OnCacheClassListener listener) {
        if (classLoader == null) {
            classLoader = context.getClassLoader();
            versionCode = version;
            File cacheFile = Objects.requireNonNull(context.getExternalFilesDir(null));
            if (cacheFile.exists() || cacheFile.mkdirs())
                classCachePath = cacheFile.getPath();
        }
        if (classCacheList == null) {
            if (SettingHelper.getInstance().isEnable(SettingHelper.dex_key))
                classCacheList = FileHelper.readFileFromSD(classCachePath + File.separator + "class-" + version);
            else
                classCacheList = new ArrayList<>();
            if (classCacheList.size() == 0) {
                new Thread(() -> getCacheClassByZip(context, version, listener)).start();
            } else
                listener.onGet();
        } else
            listener.onGet();
    }

    private static synchronized void getCacheClassByZip(Context context, int version, OnCacheClassListener listener) {
        try {
            // 不用 ZipDexContainer 因为会验证zip里面的文件是不是dex，会慢一点
            File appInstallFile = new File(context.getPackageResourcePath());
            Enumeration<? extends ZipEntry> zip = new ZipFile(appInstallFile).entries();
            while (zip.hasMoreElements()) {
                ZipEntry dexInZip = zip.nextElement();
                if (dexInZip.getName().startsWith("classes") && dexInZip.getName().endsWith(".dex")) {
                    MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry = DexFileFactory.loadDexEntry(appInstallFile, dexInZip.getName(), true, null);
                    DexBackedDexFile dexFile = dexEntry.getDexFile();
                    for (DexBackedClassDef classDef : dexFile.getClasses()) {
                        String classType = classDef.getType();
                        if (classType.contains("com/netease/cloudmusic") || classType.contains("okhttp3")) {
                            classType = classType.substring(1, classType.length() - 1).replace("/", ".");
                            classCacheList.add(classType);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileHelper.writeFileFromSD(classCachePath + File.separator + "class-" + version, classCacheList);
            listener.onGet();
        }
    }

    public interface OnCacheClassListener {
        void onGet();
    }

    public static List<String> getFilteredClasses(Pattern pattern, Comparator<String> comparator) {
        List<String> list = Stream.of(classCacheList)
                .filter(s -> pattern.matcher(s).find())
                .toList();
        Collections.sort(list, comparator);
        return list;
    }

    public static class Cookie {
        private static Class<?> clazz, abstractClazz;

        public static String getCookie() {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = getFilteredClasses(pattern, null);

                abstractClazz = Stream.of(list)
                        .map(s -> findClass(s, classLoader))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> c.getSuperclass() == Object.class)
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == ConcurrentHashMap.class))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == SharedPreferences.class))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == File.class))
                        .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == long.class))
                        .findFirst()
                        .get();

                if (versionCode >= 154) {
                    clazz = Stream.of(list)
                            .map(s -> findClass(s, classLoader))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(m -> !Modifier.isInterface(m.getModifiers()))
                            .filter(c -> c.getSuperclass() == abstractClazz)
                            .findFirst()
                            .get();
                } else {
                    clazz = abstractClazz;
                }
            }

            Object cookieString = null;
            if (versionCode >= 154) {
                //获取静态cookie方法
                Method cookieMethod = XposedHelpers.findMethodsByExactParameters(clazz, clazz)[0];
                Object cookie = XposedHelpers.callStaticMethod(clazz, cookieMethod.getName());
                for (Method method : XposedHelpers.findMethodsByExactParameters(abstractClazz, String.class)) {
                    if (method.getTypeParameters().length == 0 && method.getModifiers() == Modifier.PUBLIC) {
                        cookieString = XposedHelpers.callMethod(cookie, method.getName());
                    }
                }
            } else {
                Method cookieMethod = XposedHelpers.findMethodsByExactParameters(clazz, String.class)[0];
                cookieString = XposedHelpers.callStaticMethod(clazz, cookieMethod.getName());
            }

            return "MUSIC_U=" + cookieString;
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
                            .map(c -> findClass(c, classLoader).getDeclaredMethods())
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
                            .map(c -> findClass(c, classLoader).getDeclaredMethods())
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
                        .map(s -> findClass(s, classLoader))
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
                        .map(s -> findClass(s, classLoader))
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

    /**
     * 获取请求返回
     */
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
                        .map(s -> findClass(s, classLoader))
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

    /**
     * 获取请求URL
     */
    public static class HttpUrl {
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
                        .map(s -> findClass(s, classLoader))
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

    /**
     * 获取请求参数
     */
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
                        .map(s -> findClass(s, classLoader))
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
                Uri uri = HttpUrl.getUri(eapi);
                for (String name : uri.getQueryParameterNames()) {
                    String val = uri.getQueryParameter(name);
                    map.put(name, val != null ? val : "");
                }
                return (LinkedHashMap<String, String>) getParamsMapField().get(params);
            }
            return new LinkedHashMap<>();
        }
    }

    /**
     * 拦截器
     */
    public static class HttpInterceptor {
        private static Class<?> clazz;
        private static List<Method> methodList;

        static Class<?> getClazz() {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                if (list.isEmpty()) {
                    throw new RuntimeException("init failed");
                }

                clazz = Stream.of(list)
                        .map(s -> findClass(s, classLoader))
                        .filter(c -> c.getInterfaces().length == 1)
                        .filter(c -> Stream.of(c.getInterfaces()).anyMatch(i -> i.getName().contains("Interceptor")))
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> Modifier.isPublic(c.getModifiers()))
                        .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType().getName().contains("Pair")))
                        .findFirst()
                        .get();
            }
            return clazz;
        }

        public static List<Method> getMethodList() {
            if (methodList == null) {
                methodList = new ArrayList<>();
                methodList.addAll(Stream.of(getClazz().getDeclaredMethods())
                        .filter(m -> m.getExceptionTypes().length == 1)
                        .filter(m -> m.getParameterTypes().length == 5)
                        .filter(m -> m.getReturnType().getName().contains("Response"))
                        .toList());
            }
            return methodList;
        }
    }
}
