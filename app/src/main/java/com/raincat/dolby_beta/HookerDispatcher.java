package com.raincat.dolby_beta;

import android.content.Context;

import com.raincat.dolby_beta.hook.AdAndUpdateHook;
import com.raincat.dolby_beta.hook.AutoSignInHook;
import com.raincat.dolby_beta.hook.AutoSignInHook2;
import com.raincat.dolby_beta.hook.BlackHook;
import com.raincat.dolby_beta.hook.CommentHotClickHook;
import com.raincat.dolby_beta.hook.CookieHook;
import com.raincat.dolby_beta.hook.DownloadMD5Hook;
import com.raincat.dolby_beta.hook.EAPIHook;
import com.raincat.dolby_beta.hook.GrayHook;
import com.raincat.dolby_beta.hook.HideBubbleHook;
import com.raincat.dolby_beta.hook.HideMainBannerHook;
import com.raincat.dolby_beta.hook.HidePlaylistBannerHook;
import com.raincat.dolby_beta.hook.HideSidebarHook;
import com.raincat.dolby_beta.hook.HideTabHook;
import com.raincat.dolby_beta.hook.InternalDialogHook;
import com.raincat.dolby_beta.hook.MagiskFixHook;
import com.raincat.dolby_beta.hook.OverseaHook;
import com.raincat.dolby_beta.hook.ProfileHook;
import com.raincat.dolby_beta.hook.TaiChiFixHook;
import com.raincat.dolby_beta.hook.TestHook;
import com.raincat.dolby_beta.utils.CloudMusicPackage;
import com.raincat.dolby_beta.utils.Setting;
import com.raincat.dolby_beta.utils.Tools;

import net.androidwing.hotxposed.IHookerDispatcher;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : Hook入口
 *     version: 1.0
 * </pre>
 */

public class HookerDispatcher implements IHookerDispatcher {
    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(findClass("com.netease.cloudmusic.NeteaseMusicApplication", lpparam.classLoader),
                "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!Setting.isEnable()) {
                            return;
                        }

                        File neteaseTinkerFile = new File(Tools.neteaseTinkerPath);
                        if (neteaseTinkerFile.exists()) {
                            try {
                                String command = "chmod 200 " + neteaseTinkerFile.getAbsolutePath();
                                Runtime runtime = Runtime.getRuntime();
                                runtime.exec(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        final Context neteaseContext = (Context) param.thisObject;
                        final String processName = Tools.getCurrentProcessName(neteaseContext);

                        if (processName.equals(CloudMusicPackage.PACKAGE_NAME)) {
                            CloudMusicPackage.init(neteaseContext);
                            //黑胶
                            if (Setting.isBlackEnabled()) {
                                Tools.deleteDirectory(CloudMusicPackage.CACHE_PATH);
                                Tools.deleteDirectory(CloudMusicPackage.CACHE_PATH2);
                                new BlackHook(neteaseContext, CloudMusicPackage.versionCode);
                                new DownloadMD5Hook();
                                new EAPIHook(neteaseContext);
                            }
                            //自动签到
                            if (Setting.isAutoSignInEnabled())
                                if (CloudMusicPackage.versionCode <= 7003000)
                                    new AutoSignInHook(neteaseContext, CloudMusicPackage.versionCode);
                                else
                                    new AutoSignInHook2(neteaseContext);
                            //不变灰
                            if (Setting.isGrayEnabled())
                                new GrayHook(neteaseContext);
                            //隐藏内测弹窗
                            if (Setting.isInternalEnabled())
                                new InternalDialogHook(neteaseContext, CloudMusicPackage.versionCode);
                            //海外模式
                            if (Setting.isOverseaModeEnabled())
                                new OverseaHook(neteaseContext, CloudMusicPackage.versionCode);
                            //Magisk冲突
                            if (Setting.isMagiskEnabled())
                                new MagiskFixHook(neteaseContext);
                            //去广告与升级
                            new AdAndUpdateHook(neteaseContext, CloudMusicPackage.versionCode);
                            //修复太极优化后无法hook的bug
                            new TaiChiFixHook();
                            //获取cookie
                            if (Setting.isCookieEnabled())
                                new CookieHook(neteaseContext);
                            //精简Tab
                            if (Setting.isHideTabEnabled())
                                new HideTabHook(neteaseContext, CloudMusicPackage.versionCode);
                            //隐藏发现页Banner
                            if (Setting.isHideMainBannerEnabled())
                                new HideMainBannerHook(neteaseContext, CloudMusicPackage.versionCode);
                            //隐藏歌单页Banner
                            if (Setting.isHidePlaylistBannerEnabled())
                                new HidePlaylistBannerHook(neteaseContext, CloudMusicPackage.versionCode);
                            //隐藏小红点
                            if (Setting.isHideBubbleEnabled())
                                new HideBubbleHook(neteaseContext);
                            //打开评论后优先显示最热评论
                            if (Setting.isCommentHotEnabled())
                                new CommentHotClickHook(neteaseContext);
                            //精简侧边栏
                            if (CloudMusicPackage.versionCode <= 7003000)
                                new HideSidebarHook(neteaseContext, CloudMusicPackage.versionCode);
                            //伪装个人信息
                            new ProfileHook(neteaseContext);
                            new TestHook(neteaseContext);
                        } else if (processName.equals(CloudMusicPackage.PACKAGE_NAME + ":play")) {
                            CloudMusicPackage.init(neteaseContext);
                            if (Setting.isBlackEnabled()) {
                                new EAPIHook(neteaseContext);
                            }
                            if (Setting.isOverseaModeEnabled())
                                new OverseaHook(neteaseContext, CloudMusicPackage.versionCode);
                            new AdAndUpdateHook(neteaseContext, CloudMusicPackage.versionCode);
                            new TaiChiFixHook();
                        }
                    }
                });
    }
}
