package com.raincat.dolby_beta.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.annimon.stream.Stream;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.json.JSONObject;

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
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
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

    private static Class<?> getClassByXposed(String className) {
        Class<?> clazz = findClassIfExists(className, classLoader);
        if (clazz == null)
            clazz = findClassIfExists("com.netease.cloudmusic.NeteaseMusicApplication", classLoader);
        return clazz;
    }

    public static class Cookie {
        private static Class<?> clazz, abstractClazz;

        public static String getCookie(Context context) {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = getFilteredClasses(pattern, null);

                try {
                    abstractClazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
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
                                .map(ClassHelper::getClassByXposed)
                                .filter(c -> Modifier.isPublic(c.getModifiers()))
                                .filter(m -> !Modifier.isInterface(m.getModifiers()))
                                .filter(c -> c.getSuperclass() == abstractClazz)
                                .findFirst()
                                .get();
                    } else {
                        clazz = abstractClazz;
                    }
                } catch (NoSuchElementException e) {
                    MessageHelper.sendNotification(context, MessageHelper.cookieClassNotFoundCode);
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

    public static class DownloadTransfer {
        private static Method checkMd5Method;
        private static Method checkDownloadStatusMethod;

        //下载完后的MD5检查
        public static Method getCheckMd5Method(Context context) {
            if (checkMd5Method == null) {
                Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.transfer\\.download\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    checkMd5Method = Stream.of(list)
                            .map(c -> getClassByXposed(c).getDeclaredMethods())
                            .flatMap(Stream::of)
                            .filter(m -> m.getParameterTypes().length == 4)
                            .filter(m -> m.getParameterTypes()[0] == File.class)
                            .filter(m -> m.getParameterTypes()[1] == File.class)
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    MessageHelper.sendNotification(context, MessageHelper.transferClassNotFoundCode);
                }
            }
            return checkMd5Method;
        }

        //下载之前下载状态检查
        public static Method getCheckDownloadStatusMethod(Context context) {
            if (checkDownloadStatusMethod == null) {
                Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.transfer\\.download\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    checkDownloadStatusMethod = Stream.of(list)
                            .map(c -> getClassByXposed(c).getDeclaredMethods())
                            .flatMap(Stream::of)
                            .filter(m -> m.getReturnType() == long.class)
                            .filter(m -> m.getParameterTypes().length == 5)
                            .filter(m -> m.getParameterTypes()[1] == int.class)
                            .filter(m -> m.getParameterTypes()[3] == File.class)
                            .filter(m -> m.getParameterTypes()[4] == long.class)
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    MessageHelper.sendNotification(context, MessageHelper.transferClassNotFoundCode);
                }
            }
            return checkDownloadStatusMethod;
        }
    }

    public static class MainActivitySuperClass {
        private static Class<?> clazz;
        private static List<Method> methods;
        private static Method method;

        static void getClazz(Context context) {
            if (clazz == null) {
                Class<?> mainActivityClass = findClass("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader());
                clazz = mainActivityClass.getSuperclass();
            }
        }

        public static List<Method> getTabItemStringMethods(Context context) {
            if (clazz == null)
                getClazz(context);
            if (methods == null && clazz != null) {
                List<Method> methodList = Arrays.asList(clazz.getDeclaredMethods());
                methods = Stream.of(methodList)
                        .filter(m -> m.getParameterTypes().length >= 1)
                        .filter(m -> m.getReturnType() == void.class)
                        .filter(m -> m.getParameterTypes()[0] == String[].class)
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .toList();
            }
            return methods;
        }

        public static Method getViewPagerInitMethod(Context context) {
            if (method == null) {
                try {
                    List<Method> methodList = Arrays.asList(findClass("com.netease.cloudmusic.activity.MainActivity", context.getClassLoader()).getDeclaredMethods());
                    method = Stream.of(methodList)
                            .filter(m -> m.getParameterTypes().length == 1)
                            .filter(m -> m.getReturnType() == void.class)
                            .filter(m -> m.getParameterTypes()[0] == Intent.class)
                            .filter(m -> Modifier.isPrivate(m.getModifiers()))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.tabClassNotFoundCode);
                }
            }
            return method;
        }
    }

    public static class BottomTabView {
        private static Class<?> clazz;
        private static Method initMethod, refreshMethod;

        public static Class<?> getClazz(Context context) {
            if (clazz == null) {
                try {
                    Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.[a-z0-9]{1,2}\\.[a-z]$");
                    Pattern pattern2 = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z0-9]{1,2}\\.[a-z]\\.[a-z]$");
                    Pattern pattern3 = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.main\\.[a-z]$");
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    list.addAll(ClassHelper.getFilteredClasses(pattern2, Collections.reverseOrder()));
                    list.addAll(ClassHelper.getFilteredClasses(pattern3, Collections.reverseOrder()));
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(m -> Modifier.isFinal(m.getModifiers()))
                            .filter(m -> !Modifier.isInterface(m.getModifiers()))
                            .filter(m -> !Modifier.isStatic(m.getModifiers()))
                            .filter(m -> !Modifier.isAbstract(m.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == String.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == ArrayList.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == boolean.class))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType() == ArrayList.class && Modifier.isFinal(m.getModifiers()) && m.getParameterTypes().length == 0))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType() == String[].class && Modifier.isFinal(m.getModifiers()) && m.getParameterTypes().length == 0))
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    MessageHelper.sendNotification(context, MessageHelper.tabClassNotFoundCode);
                }
            }
            return clazz;
        }

        public static Method getTabInitMethod(Context context) {
            if (initMethod == null) {
                Method[] methods = findMethodsByExactParameters(clazz, ArrayList.class);
                if (methods.length != 0)
                    initMethod = methods[0];
                else
                    MessageHelper.sendNotification(context, MessageHelper.tabClassNotFoundCode);
            }
            return initMethod;
        }

        public static Method getTabRefreshMethod(Context context) {
            if (refreshMethod == null) {
                Method[] methods = findMethodsByExactParameters(clazz, void.class, List.class);
                if (methods.length != 0)
                    refreshMethod = methods[0];
                else
                    MessageHelper.sendNotification(context, MessageHelper.tabClassNotFoundCode);
            }
            return refreshMethod;
        }
    }

    public static class SidebarItem {
        private static Class<?> clazz;

        public static Class<?> getClazz(Context context) {
            if (clazz == null) {
                try {
                    Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.account\\.[a-z]$");
                    Pattern pattern2 = Pattern.compile("^com\\.netease\\.cloudmusic\\.music\\.biz\\.sidebar\\.account\\.[a-z]$");
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    list.addAll(ClassHelper.getFilteredClasses(pattern2, Collections.reverseOrder()));
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(m -> Modifier.isFinal(m.getModifiers()))
                            .filter(m -> !Modifier.isInterface(m.getModifiers()))
                            .filter(m -> !Modifier.isStatic(m.getModifiers()))
                            .filter(m -> !Modifier.isAbstract(m.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == int.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == List.class))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType() == List.class))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType() == Throwable.class))
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    MessageHelper.sendNotification(context, MessageHelper.sidebarClassNotFoundCode);
                }
            }
            return clazz;
        }
    }

    /**
     * 评论
     */
    public static class CommentDataClass {
        private static Class<?> clazz;

        public static Class<?> getClazz() {
            if (clazz == null) {
                try {
                    Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.comment2\\.[a-z]\\.[a-z]$");
                    Pattern pattern2 = Pattern.compile("^com\\.netease\\.cloudmusic\\.music\\.biz\\.comment\\.[a-z]\\.[a-z]$");
                    Pattern pattern3 = Pattern.compile("^com\\.netease\\.cloudmusic\\.music\\.biz\\.comment\\.viewmodel\\.[a-z]$");
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    list.addAll(ClassHelper.getFilteredClasses(pattern2, Collections.reverseOrder()));
                    list.addAll(ClassHelper.getFilteredClasses(pattern3, Collections.reverseOrder()));
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(m -> !Modifier.isInterface(m.getModifiers()))
                            .filter(m -> !Modifier.isStatic(m.getModifiers()))
                            .filter(m -> !Modifier.isAbstract(m.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == int.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == List.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == Intent.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == String.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == long.class))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == boolean.class))
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
            }
            return clazz;
        }
    }

    /**
     * 广告
     */
    public static class Ad {
        private static Class<?> adClazz;
        private static Class<?> clazz;

        public static Class<?> getClazz() {
            if (clazz == null) {
                adClazz = getClassByXposed("com.netease.cloudmusic.meta.Ad");
                try {
                    Pattern pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.module\\.ad\\.[a-z]$");
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(m -> !Modifier.isInterface(m.getModifiers()))
                            .filter(m -> !Modifier.isStatic(m.getModifiers()))
                            .filter(m -> !Modifier.isAbstract(m.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType().getName().contains("VideoAdInfo")))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType() == adClazz))
                            .findFirst()
                            .get();
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
            }
            return clazz;
        }

        public static List<Method> getAdMethod() {
            try {
                List<Method> methodList = Arrays.asList(getClazz().getDeclaredMethods());
                List<Method> hookMethodList = Stream.of(methodList)
                        .filter(m -> m.getReturnType().getName().contains("com.netease.cloudmusic.meta"))
                        .filter(m -> Stream.of(m.getParameterTypes()).anyMatch(c -> c == JSONObject.class))
                        .toList();
                hookMethodList.addAll(Stream.of(methodList)
                        .filter(m -> Stream.of(m.getParameterTypes()).anyMatch(c -> c.getName().contains("com.netease.cloudmusic.meta")))
                        .filter(m -> Stream.of(m.getParameterTypes()).anyMatch(c -> c == JSONObject.class))
                        .toList());
                return hookMethodList;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class OKHttp3Response {
        private static Class<?> clazz;

        final Object okHttp3Response;

        public OKHttp3Response(Object okHttp3Response) {
            this.okHttp3Response = okHttp3Response;
        }

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern = Pattern.compile("^okhttp3\\.[a-zA-Z]{1,8}$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
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
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        public Object getHeadersObject(Context context) throws IllegalAccessException, NullPointerException {
            Field[] fields = getClazz(context).getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(f -> Stream.of(f.getType()).anyMatch(pf -> pf == OKHttp3Header.getClazz(context)))
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

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern = Pattern.compile("^okhttp3\\.[a-zA-Z]{1,7}$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> Modifier.isFinal(c.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == String[].class))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        public String[] getHeaders(Context context) throws IllegalAccessException, NullPointerException {
            Field[] fields = getClazz(context).getDeclaredFields();
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

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> Modifier.isFinal(c.getModifiers()))
                            .filter(c -> c.getSuperclass() == Object.class)
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == OKHttp3Response.getClazz(context)))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        public Object getResponseObject(Context context) throws IllegalAccessException, NullPointerException {
            Field[] fields = getClazz(context).getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(f -> Stream.of(f.getType().getInterfaces()).anyMatch(i -> i == Closeable.class))
                    .filter(f -> Stream.of(f.getType().getDeclaredFields()).anyMatch(pf -> pf.getType().getName().startsWith("okhttp3")))
                    .findFirst().get();

            dataField.setAccessible(true);
            return dataField.get(httpResponse);
        }

        public Object getEapi(Context context) throws IllegalAccessException, NullPointerException {
            Field[] fields = getClazz(context).getDeclaredFields();
            Field dataField = Stream.of(fields)
                    .filter(c -> Modifier.isAbstract(c.getType().getModifiers()))
                    .filter(c -> c.getType().getSuperclass() == Object.class)
                    .filter(c -> Stream.of(c.getType().getDeclaredFields()).anyMatch(m -> m.getType().getName().startsWith("okhttp3")))
                    .findFirst().get();

            dataField.setAccessible(true);
            return dataField.get(httpResponse);
        }

        public static Method getResultMethod(Context context) {
            if (getResultMethod == null) {
                try {
                    List<Method> methodList = Arrays.asList(getClazz(context).getDeclaredMethods());
                    getResultMethod = Stream.of(methodList)
                            .filter(m -> m.getExceptionTypes().length == 2)
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return getResultMethod;
        }
    }

    /**
     * 获取请求URL
     */
    public static class HttpUrl {
        private static Class<?> clazz;

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> c.getSuperclass() == Object.class)
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType().getName().startsWith("okhttp3")))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        public static Uri getUri(Context context, Object eapi) throws IllegalAccessException, NullPointerException {
            Field uriField = XposedHelpers.findFirstFieldByExactType(getClazz(context), Uri.class);
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

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]\\.[a-z]$");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]\\.[a-z]\\.[a-z]$");
                List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());

                try {
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> Stream.of(c.getInterfaces()).anyMatch(i -> i == Serializable.class))
                            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredFields()).anyMatch(m -> m.getType() == LinkedHashMap.class))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        static Field getParamsMapField(Context context) {
            if (paramsMap == null) {
                Field[] fields = getClazz(context).getDeclaredFields();
                paramsMap = Stream.of(fields)
                        .filter(c -> Stream.of(c.getType()).anyMatch(m -> m == LinkedHashMap.class))
                        .findFirst().get();
                paramsMap.setAccessible(true);
            }
            return paramsMap;
        }

        public static LinkedHashMap<String, String> getParams(Context context, Object eapi) throws IllegalAccessException, NullPointerException {
            List<Method> list = new ArrayList<>(Arrays.asList(findMethodsByExactParameters(eapi.getClass(), getClazz(context))));
            if (list != null && list.size() != 0) {
                Object params = XposedHelpers.callMethod(eapi, list.get(0).getName());
                LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) getParamsMapField(context).get(params);
                Uri uri = HttpUrl.getUri(context, eapi);
                for (String name : uri.getQueryParameterNames()) {
                    String val = uri.getQueryParameter(name);
                    map.put(name, val != null ? val : "");
                }
                return (LinkedHashMap<String, String>) getParamsMapField(context).get(params);
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

        static Class<?> getClazz(Context context) {
            if (clazz == null) {
                Pattern pattern;
                if (versionCode < 154)
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.[a-z]\\.[a-z]\\.[a-z]");
                else
                    pattern = Pattern.compile("^com\\.netease\\.cloudmusic\\.network\\.[a-z]");
                try {
                    List<String> list = ClassHelper.getFilteredClasses(pattern, Collections.reverseOrder());
                    clazz = Stream.of(list)
                            .map(ClassHelper::getClassByXposed)
                            .filter(c -> c.getInterfaces().length == 1)
                            .filter(c -> Stream.of(c.getInterfaces()).anyMatch(i -> i.getName().contains("Interceptor")))
                            .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                            .filter(c -> Modifier.isPublic(c.getModifiers()))
                            .filter(c -> Stream.of(c.getDeclaredMethods()).anyMatch(m -> m.getReturnType().getName().contains("Pair")))
                            .findFirst()
                            .get();
                } catch (Exception e) {
                    MessageHelper.sendNotification(context, MessageHelper.coreClassNotFoundCode);
                }
            }
            return clazz;
        }

        public static List<Method> getMethodList(Context context) {
            if (methodList == null) {
                methodList = new ArrayList<>();
                methodList.addAll(Stream.of(getClazz(context).getDeclaredMethods())
                        .filter(m -> m.getExceptionTypes().length == 1)
                        .filter(m -> m.getParameterTypes().length == 5)
                        .filter(m -> m.getReturnType().getName().contains("Response"))
                        .toList());
            }
            return methodList;
        }
    }
}
