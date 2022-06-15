package com.raincat.dolby_beta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;

import com.raincat.dolby_beta.helper.ClassHelper;
import com.raincat.dolby_beta.helper.ExtraHelper;
import com.raincat.dolby_beta.helper.FileHelper;
import com.raincat.dolby_beta.helper.NotificationHelper;
import com.raincat.dolby_beta.helper.SettingHelper;
import com.raincat.dolby_beta.hook.AdAndUpdateHook;
import com.raincat.dolby_beta.hook.AdExtraHook;
import com.raincat.dolby_beta.hook.AutoSignInHook;
import com.raincat.dolby_beta.hook.BlackHook;
import com.raincat.dolby_beta.hook.CdnHook;
import com.raincat.dolby_beta.hook.CommentHotClickHook;
import com.raincat.dolby_beta.hook.DownloadMD5Hook;
import com.raincat.dolby_beta.hook.EAPIHook;
import com.raincat.dolby_beta.hook.GrayHook;
import com.raincat.dolby_beta.hook.HideBannerHook;
import com.raincat.dolby_beta.hook.HideBubbleHook;
import com.raincat.dolby_beta.hook.HideSidebarHook;
import com.raincat.dolby_beta.hook.HideTabHook;
import com.raincat.dolby_beta.hook.InternalDialogHook;
import com.raincat.dolby_beta.hook.LoginFixHook;
import com.raincat.dolby_beta.hook.MagiskFixHook;
import com.raincat.dolby_beta.hook.NightModeHook;
import com.raincat.dolby_beta.hook.PlayerActivityHook;
import com.raincat.dolby_beta.hook.ProxyHook;
import com.raincat.dolby_beta.hook.SettingHook;
import com.raincat.dolby_beta.hook.UserProfileHook;
import com.raincat.dolby_beta.hook.ListentogetherHook;
import com.raincat.dolby_beta.utils.Tools;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2021/09/22
 *     desc   : hook入口
 *     version: 1.0
 * </pre>
 */

public class Hook {
    private final static String PACKAGE_NAME = "com.netease.cloudmusic";
    //进程初始化状态
    public boolean playProcessInit = false;
    public boolean mainProcessInit = false;
    //主线程反编译dex完成后通知可以对play进程进行hook了
    private final String msg_hook_play_process = "hookPlayProcess";
    //play进程初始化完成通知主线程
    private final String msg_play_process_init_finish = "playProcessInitFinish";
    //发通知
    public static final String msg_send_notification = "sendNotification";

    public Hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.netease.cloudmusic.NeteaseMusicApplication", lpparam.classLoader),
                "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final Context context = (Context) param.thisObject;
                        final int versionCode = context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0).versionCode;
                        //初始化仓库
                        ExtraHelper.init(context);
                        //初始化设置
                        SettingHelper.init(context);

                        final String processName = Tools.getCurrentProcessName(context);
                        if (processName.equals(PACKAGE_NAME)) {
                            //设置
                            new SettingHook(context, versionCode);
                            //总开关
                            if (!SettingHelper.getInstance().getSetting(SettingHelper.master_key))
                                return;
                            //音源代理
                            new ProxyHook(context, false);
                            //黑胶
                            if (SettingHelper.getInstance().isEnable(SettingHelper.black_key)) {
                                new BlackHook(context, versionCode);
                                deleteAdAndTinker();
                            }
                            //一起听
                            if (SettingHelper.getInstance().isEnable(SettingHelper.listen_key)) {
                                new ListentogetherHook(context, versionCode);

                            }
                            //不变灰
                            new GrayHook(context);
                            //自动签到
                            new AutoSignInHook(context, versionCode);
                            //去广告与去升级
                            new AdAndUpdateHook(context, versionCode);
                            //修复magisk冲突导致的无法读写外置sd卡
                            new MagiskFixHook(context);
                            //去掉内测与听歌识曲弹窗
                            new InternalDialogHook(context, versionCode);
                            //修复登录失败
                            new LoginFixHook(context);
//                            new TestHook(context);
                            ClassHelper.getCacheClassList(context, versionCode, () -> {
                                //获取账号信息
                                new UserProfileHook(context);
                                //网络访问
                                new EAPIHook(context);
                                //下载MD5校验
                                new DownloadMD5Hook(context);
                                //夜间模式
                                new NightModeHook(context, versionCode);
                                //精简tab
                                new HideTabHook(context, versionCode);
                                //精简侧边栏
                                new HideSidebarHook(context, versionCode);
                                //移除Banner
                                new HideBannerHook(context, versionCode);
                                //隐藏小红点
                                new HideBubbleHook(context);
                                //黑胶停转，隐藏K歌按钮
                                new PlayerActivityHook(context, versionCode);
                                //打开评论后优先显示最热评论
                                new CommentHotClickHook(context);
                                //绕过CDN责任链拦截器检测
                                new CdnHook(context, versionCode);
                                //广告移除增强
                                new AdExtraHook();

                                mainProcessInit = true;
                                if (mainProcessInit && playProcessInit)
                                    context.sendBroadcast(new Intent(msg_hook_play_process));
                            });
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(msg_play_process_init_finish);
                            intentFilter.addAction(msg_send_notification);
                            context.registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context c, Intent intent) {
                                    if (msg_play_process_init_finish.equals(intent.getAction())) {
                                        playProcessInit = true;
                                        if (mainProcessInit && playProcessInit)
                                            context.sendBroadcast(new Intent(msg_hook_play_process));
                                    } else if (msg_send_notification.equals(intent.getAction())
                                            && SettingHelper.getInstance().isEnable(SettingHelper.warn_key)) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            NotificationHelper.getInstance(context).sendUnLockNotification(context, intent.getIntExtra("code", 0x10),
                                                    intent.getStringExtra("title"), intent.getStringExtra("title"), intent.getStringExtra("message"));
                                        XposedBridge.log(intent.getStringExtra("title") + "：" + intent.getStringExtra("message"));
                                    }
                                }
                            }, intentFilter);
                        } else if (processName.equals(PACKAGE_NAME + ":play") && SettingHelper.getInstance().getSetting(SettingHelper.master_key)) {
                            //音源代理
                            new ProxyHook(context, true);
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(msg_hook_play_process);
                            context.registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context c, Intent intent) {
                                    if (msg_hook_play_process.equals(intent.getAction())) {
                                        ClassHelper.getCacheClassList(context, versionCode, () -> {
                                            new EAPIHook(context);
                                            new CdnHook(context, versionCode);
                                        });
                                    }
                                }
                            }, intentFilter);
                            context.sendBroadcast(new Intent(msg_play_process_init_finish));
                        }
                    }
                });

        //关闭tinker
        Class<?> tinkerClass = XposedHelpers.findClassIfExists("com.tencent.tinker.loader.app.TinkerApplication", lpparam.classLoader);
        if (tinkerClass != null)
            XposedBridge.hookAllConstructors(tinkerClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.args[0] = 0;
                }
            });
    }

    /**
     * 删掉广告和热修复
     */
    private void deleteAdAndTinker() throws IOException {
        //广告缓存路径
        String CACHE_PATH = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Ad";
        String CACHE_PATH2 = Environment.getExternalStorageDirectory() + "/Android/data/com.netease.cloudmusic/cache/Ad";

        String TINKER_PATH = "data/data/" + PACKAGE_NAME + "/tinker";

        FileHelper.deleteDirectory(CACHE_PATH);
        FileHelper.deleteDirectory(CACHE_PATH2);


        File tinkerFile = new File(TINKER_PATH);
        if (tinkerFile.exists() && tinkerFile.isDirectory())
            FileHelper.deleteDirectory(TINKER_PATH);
        if (!tinkerFile.exists())
            tinkerFile.createNewFile();

        String command = "chmod 000 " + tinkerFile.getAbsolutePath();
        Runtime runtime = Runtime.getRuntime();
        runtime.exec(command);
    }
}
