package com.raincat.dolby_beta.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/16
 *     desc   : 歌曲打卡
 *     version: 1.0
 * </pre>
 */
public class SignDao {
    static final String TABLE_NAME = "signed_song";
    static final String SIGNED_USER_ID = "signed_user_id";
    static final String SIGNED_SONG_ID = "signed_song_id";

    private SignDbOpenHelper dbHelper;
    static private SignDao dao;

    private SignDao(Context context) {
        dbHelper = SignDbOpenHelper.getInstance(context);
    }

    public static synchronized SignDao getInstance(Context context) {
        if (dao == null) {
            dao = new SignDao(context);
        }
        return dao;
    }

    /**
     * 保存已打卡歌曲记录
     */
    public void saveSongList(List<Long> songIdList, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            for (long songId : songIdList) {
                ContentValues values = new ContentValues();
                values.put(SIGNED_SONG_ID, songId);
                values.put(SIGNED_USER_ID, userId);
                db.replace(TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    /**
     * 获取已打卡歌曲
     */
    public HashMap<Long, Integer> getSong(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        HashMap<Long, Integer> longMap = new HashMap<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select " + SIGNED_SONG_ID + " from " + TABLE_NAME + " where " + SIGNED_USER_ID + " = '" + userId + "'", null);
            while (cursor.moveToNext()) {
                longMap.put(cursor.getLong(cursor.getColumnIndex(SIGNED_SONG_ID)), 1);
            }
            cursor.close();
        }
        db.close();
        return longMap;
    }

    /**
     * 删除一个人的某条额外记录
     */
    public void deleteSong(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME, SIGNED_USER_ID + " = ?", new String[]{userId});
        }
        db.close();
    }
}