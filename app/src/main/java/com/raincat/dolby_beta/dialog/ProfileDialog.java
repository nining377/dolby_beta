package com.raincat.dolby_beta.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.raincat.dolby_beta.R;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/14
 *     desc   : 个人中心弹窗
 *     version: 1.0
 * </pre>
 */
public class ProfileDialog {
    private static AlertDialog dialog;
    private static int role;

    public interface OnClickListener {
        void onClick();
    }

    public static void showProfileDialog(Context context, final SharedPreferences share, final OnClickListener clickListener) {
        if (dialog != null)
            return;

        int level = share.getInt("level", -1);
        int follows = share.getInt("follows", -1);
        int fans = share.getInt("fans", -1);
        role = share.getInt("role", -1);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_profile_item, null);
        final EditText et_level =  view.findViewById(R.id.et_level);
        final EditText et_follows =  view.findViewById(R.id.et_follows);
        final EditText et_fans =  view.findViewById(R.id.et_fans);
        final RadioGroup rg_role =  view.findViewById(R.id.rg_role);

        if (level != -1)
            et_level.setText(level + "");
        if (follows != -1)
            et_follows.setText(follows + "");
        if (fans != -1)
            et_fans.setText(fans + "");
        if (role == 4)
            rg_role.check(R.id.rb_role_b);
        else if (role == 100)
            rg_role.check(R.id.rb_role_c);
        else if (role == 200)
            rg_role.check(R.id.rb_role_d);
        else if (role == 300)
            rg_role.check(R.id.rb_role_e);

        rg_role.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_role_a:
                    role = -1;
                    break;
                case R.id.rb_role_b:
                    role = 4;
                    break;
                case R.id.rb_role_c:
                    role = 99;
                    break;
                case R.id.rb_role_d:
                    role = 200;
                    break;
                case R.id.rb_role_e:
                    role = 300;
                    break;
            }
        });

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setTitle("设置个人信息")
                .setMessage("注意：\n1.太极可能不生效\n2.设置的个人信息仅自己可见，留空则不修改该项")
                .setNeutralButton("恢复默认值", null)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialogInterface, i) -> dialog = null).show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String levelString = et_level.getText().toString();
            String followsString = et_follows.getText().toString();
            String fansString = et_fans.getText().toString();

            if (levelString.length() == 2 && !levelString.equals("10")) {
                et_level.setFocusableInTouchMode(true);
                et_level.setFocusable(true);
                et_level.requestFocus();
                et_level.setError("最高等级为10！");
                return;
            }

            if (levelString.length() == 0)
                share.edit().putInt("level", -1).apply();
            else
                share.edit().putInt("level", Integer.parseInt(levelString)).apply();
            if (followsString.length() == 0)
                share.edit().putInt("follows", -1).apply();
            else
                share.edit().putInt("follows", Integer.parseInt(followsString)).apply();
            if (fansString.length() == 0)
                share.edit().putInt("fans", -1).apply();
            else
                share.edit().putInt("fans", Integer.parseInt(fansString)).apply();
            share.edit().putInt("role", role).apply();

            if (clickListener != null)
                clickListener.onClick();

            dialog.dismiss();
            dialog = null;
        });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            et_level.setText("");
            et_follows.setText("");
            et_fans.setText("");
            rg_role.check(R.id.rb_role_a);
            role = -1;
        });
    }
}
