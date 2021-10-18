# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Bin\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class net.androidwing.hotxposed.* {*;}
-keep class com.raincat.dolby_beta.MainHook
-keep class com.raincat.dolby_beta.helper.ScriptHelper

# 跳过所有Json实体类
-keep public class **.*model*.** {*;}

-keep class com.raincat.dolby_beta.HookerDispatcher* {
  void dispatch(*);
}

-keep public class android.app.**
-keep class com.gyf.barlibrary.* {*;}
-dontwarn com.gyf.barlibrary.**

-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn javax.lang.model.element.Modifier
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn android.app.**
-dontwarn org.jf.dexlib2.dexbacked.**

#混淆变量和函数
-obfuscationdictionary proguard-class.txt
#混淆类名
-classobfuscationdictionary proguard-class.txt
# 指定class
-packageobfuscationdictionary proguard-class.txt
# 将包里的类混淆成n个再重新打包到一个统一的package中  会覆盖flattenpackagehierarchy选项
-repackageclasses com.raincat.dolby_beta
# 删除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
}