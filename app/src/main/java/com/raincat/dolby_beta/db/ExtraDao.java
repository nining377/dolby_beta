package com.raincat.dolby_beta.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/03/30
 *     desc   : 额外信息
 *     version: 1.0
 * </pre>
 */
public class ExtraDao {
    static final String TABLE_NAME = "extra";
    static final String EXTRA_KEY = "extra_key";
    static final String EXTRA_VALUE = "extra_value";

    private ExtraDbOpenHelper dbHelper;
    static private ExtraDao dao;

    private ExtraDao(Context context) {
        dbHelper = ExtraDbOpenHelper.getInstance(context);
    }

    public static synchronized ExtraDao getInstance() {
        return dao;
    }

    public static void init(Context context) {
        dao = new ExtraDao(context);
    }

    /**
     * 保存额外记录
     */
    public synchronized void saveExtra(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(EXTRA_KEY, key);
            values.put(EXTRA_VALUE, value);
            db.replace(TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * 获取某个额外记录
     */
    public synchronized String getExtra(String key) {
        String extra = "-1";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + EXTRA_KEY + " = '" + key + "'", null);
            if (cursor.moveToNext())
                extra = cursor.getString(cursor.getColumnIndex(EXTRA_VALUE));
            cursor.close();
        }
        db.close();
        return extra;
    }

    /**
     * 删除一个人的某条额外记录
     */
    public synchronized void deleteExtra(String key) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME, EXTRA_KEY + " = ? ", new String[]{key});
        }
        db.close();
    }

    /**
     * 删除所有额外记录
     */
    public synchronized void deleteAllExtra() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME, null, null);
        }
        db.close();
    }
}
