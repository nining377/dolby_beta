package com.raincat.dolby_beta.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/03/24
 *     desc   : 云盘歌曲缓存
 *     version: 1.0
 * </pre>
 */
public class CloudDao {
    static final String TABLE_NAME = "cloud";
    static final String SONG_ID = "song_id";
    static final String SONG_VALUE = "song_value";

    private CloudDbOpenHelper dbHelper;
    static private CloudDao dao;

    private CloudDao(Context context) {
        dbHelper = CloudDbOpenHelper.getInstance(context);
    }

    public static synchronized CloudDao getInstance(Context context) {
        if (dao == null) {
            dao = new CloudDao(context);
        }
        return dao;
    }

    /**
     * 保存歌曲记录
     */
    public void saveSong(int id, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(SONG_ID, id);
            values.put(SONG_VALUE, value);
            db.replace(TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * 获取某个歌曲记录
     */
    public String getSong(int id) {
        String song = "";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + SONG_ID + " = " + id, null);
            if (cursor.moveToNext()) {
                song = cursor.getString(cursor.getColumnIndex(SONG_VALUE));
            }
            deleteSong(id);
            cursor.close();
        }
        db.close();
        return song;
    }

    /**
     * 删除一个人的某条歌曲记录
     */
    private void deleteSong(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME, SONG_ID + " = ?", new String[]{id + ""});
        }
        db.close();
    }

    /**
     * 删除所有歌曲记录
     */
    public void deleteAllSong() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME, null, null);
        }
        db.close();
    }
}
