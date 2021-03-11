package com.raincat.dolby_beta.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.raincat.dolby_beta.BuildConfig;
import com.raincat.dolby_beta.R;
import com.raincat.dolby_beta.dialog.ProfileDialog;
import com.raincat.dolby_beta.dialog.SidebarCutDialog;
import com.raincat.dolby_beta.dialog.SignSongDialog;
import com.raincat.dolby_beta.utils.CloudMusicPackage;
import com.raincat.dolby_beta.utils.Tools;

import java.io.File;


public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final int SCRIPT = 0x10, NODE = 0x11;
    private Context context;

    private SharedPreferences share;
    private int authorCheckCount = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                getPackageManager().setComponentEnabledSetting(new ComponentName(MainActivity.this, context.getClass().getName() + "Alias"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else if (msg.what == 1) {
                getPackageManager().setComponentEnabledSetting(new ComponentName(MainActivity.this, context.getClass().getName() + "Alias"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setWorldReadable();

        context = this;
        share = PreferenceManager.getDefaultSharedPreferences(context);
        share.edit().remove("cookie").apply();
        findPreference(getString(R.string.core_ver_key)).setSummary(getString(R.string.core_ver_summary) + BuildConfig.VERSION_NAME);

        findPreference(getString(R.string.script_key)).setSummary(share.getString("script", "script.zip"));
        findPreference(getString(R.string.node_key)).setSummary(share.getString("node", "node"));

        checkState();
        checkSignature();

        setProfile();
        addListener();
        removeDivider();
    }

    /**
     * 去掉下划线
     */
    private void removeDivider() {
        ListView listView = findViewById(android.R.id.list);
        listView.setDivider(null);
    }

    private void setProfile() {
        int level = share.getInt("level", -1);
        int follows = share.getInt("follows", -1);
        int fans = share.getInt("fans", -1);
        int role = share.getInt("role", -1);
        String roleString = "默认";
        if (role == 4)
            roleString = "网易音乐人";
        else if (role == 99)
            roleString = "大V（V标）";
        else if (role == 200)
            roleString = "达人（黄色星星）";
        else if (role == 300)
            roleString = "管理员（蓝色星星）";

        String profile = "等级：%s" +
                "\n关注：%s" +
                "\n粉丝：%s" +
                "\n角色：%s";

        findPreference(getString(R.string.profile_key)).setSummary(String.format(profile, level == -1 ? "默认" : level, follows == -1 ? "默认" : follows, fans == -1 ? "默认" : fans, roleString));
    }

    private void addListener() {
        CheckBoxPreference hide_icon = (CheckBoxPreference) findPreference(getString(R.string.hide_module_icon_key));
        hide_icon.setOnPreferenceChangeListener((preference, o) -> {
            boolean state = (Boolean) o;
            handler.sendEmptyMessageDelayed(state ? 0 : 1, 1000);
            return true;
        });

        Preference self_sign = findPreference(getString(R.string.self_sign_key));
        self_sign.setOnPreferenceClickListener(preference -> {
            SignSongDialog.showSignSongDialog(context, share);
            return false;
        });

        Preference hide_sidebar = findPreference(getString(R.string.hide_sidebar_key));
        hide_sidebar.setOnPreferenceClickListener(preference -> {
            SidebarCutDialog.showSidebarCutDialog(context, share);
            return false;
        });

        Preference profile = findPreference(getString(R.string.profile_key));
        profile.setOnPreferenceClickListener(preference -> {
            ProfileDialog.showProfileDialog(context, share, this::setProfile);
            return false;
        });

        Preference update = findPreference(getString(R.string.github_key));
        update.setOnPreferenceClickListener(preference -> {
            Uri uri = Uri.parse("https://github.com/nining377/dolby_beta");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return false;
        });

        Preference script = findPreference(getString(R.string.script_key));
        script.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .putExtra(Intent.EXTRA_MIME_TYPES, "application/zip")
                    .setType("*/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, SCRIPT);
            return false;
        });

        Preference node = findPreference(getString(R.string.node_key));
        node.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("*/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, NODE);
            return false;
        });

        Preference ver = findPreference(getString(R.string.core_ver_key));
        ver.setOnPreferenceClickListener(preference -> {
            authorCheckCount++;
            if (authorCheckCount == 5) {
                showMessageDialog("获取网易云cookie", "请勿关闭该弹窗，重启网易云并重新登录账号直至提示获取cookie成功后再关闭");
                share.edit().putBoolean("cookie", true).apply();
                authorCheckCount = 0;
            }
            return false;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (!s.equals("time") && !s.equals("notice") && !s.equals("base_url") && !s.equals("cookie")
                && !s.equals("Cookie") && !s.equals("playlist_id") && !s.equals("start") && !s.equals("end")) {
            if (!s.equals(getString(R.string.hide_module_icon_key))) {
                Toast.makeText(this, "操作成功，请重启网易云音乐！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "操作成功！", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 检测xposed状态
     */
    private void checkState() {
        String method = null;
        if (isModuleActive()) {
            method = "Xposed / EdXposed";
        } else if (isVXP()) {
            method = "VirtualXposed";
        } else if (isExpModuleActive()) {
            method = "太极";
        }
        if (method == null)
            showMessageDialog("引导", getString(R.string.Module_is_Not_Active));
    }

    private boolean isExpModuleActive() {
        boolean isExp = false;
        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = contentResolver.call(uri, "active", null, null);
            if (result == null) {
                return false;
            }
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    private static boolean isModuleActive() {
        return false;
    }

    private boolean isVXP() {
        return System.getProperty("vxp") != null;
    }

    private void showMessageDialog(final String title, final String message) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("确定", (dialogInterface, i) -> share.edit().remove("cookie").apply()).show();
    }


    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    @SuppressLint({"SetWorldReadable", "WorldReadableFiles"})
    private void setWorldReadable() {
        File prefsFile = new File(getApplicationInfo().dataDir + File.separator + "shared_prefs"
                + File.separator + getPreferenceManager().getSharedPreferencesName() + ".xml");
        if (prefsFile.exists()) {
            prefsFile.setReadable(true, false);
            prefsFile.setExecutable(true, false);
            prefsFile.setWritable(true, true);
        }
    }

    private void checkSignature() {
        String sign = Tools.getSign(context, CloudMusicPackage.PACKAGE_NAME);
        if (sign.length() == 0) {
            findPreference(getString(R.string.unblock_key)).setSummary("注意：你还未安装或正在使用大喇叭不支持的网易云音乐版本");
        } else if (sign.equals(CloudMusicPackage.TAICHI_PACKAGE_SIGN)) {
            findPreference(getString(R.string.unblock_key)).setSummary("注意：你正在使用太极阴，若发现模块失效请清除网易云音乐数据");
        } else if (!sign.equals(CloudMusicPackage.PACKAGE_SIGN)) {
            findPreference(getString(R.string.unblock_key)).setSummary("注意：你正在使用非官方版网易云音乐，这将可能导致部分功能失效");
            showMessageDialog("注意", "你正在使用非官方版网易云音乐，这将可能导致部分功能失效");
        } else {
            findPreference(getString(R.string.unblock_key)).setSummary("注意：若发现模块失效请清除网易云音乐数据");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            if (requestCode == NODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    share.edit().putString("node", uri.getPath()).apply();
                    findPreference(getString(R.string.node_key)).setSummary(uri.getPath());
                }
            } else if (requestCode == SCRIPT) {
                Uri uri = data.getData();
                if (uri != null && uri.getPath() != null && uri.getPath().endsWith("zip")) {
                    share.edit().putString("script", uri.getPath()).apply();
                    findPreference(getString(R.string.script_key)).setSummary(uri.getPath());
                } else
                    Toast.makeText(context, "你选择的不是ZIP文件！", Toast.LENGTH_SHORT).show();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        setWorldReadable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
