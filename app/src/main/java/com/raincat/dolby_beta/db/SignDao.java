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
    static final String TABLE_NAME_1 = "signed_song";
    static final String TABLE_NAME_2 = "signed_list";
    static final String SIGNED_USER_ID = "signed_user_id";
    static final String SIGNED_SONG_ID = "signed_song_id";
    static final String SIGNED_LIST_ID = "signed_list_id";

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
    public void saveSongList(List<Long> songIdList, long listId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            for (long songId : songIdList) {
                ContentValues values = new ContentValues();
                values.put(SIGNED_SONG_ID, songId);
                values.put(SIGNED_LIST_ID, listId);
                values.put(SIGNED_USER_ID, userId);
                db.replace(TABLE_NAME_1, null, values);
            }
        }
        db.close();
    }

    /**
     * 保存已打卡歌单记录
     */
    public void saveList(long listId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(SIGNED_LIST_ID, listId);
            values.put(SIGNED_USER_ID, userId);
            db.replace(TABLE_NAME_2, null, values);
        }
        db.close();
    }

    /**
     * 获取已打卡歌单
     */
    public HashMap<Long, Integer> getList(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        HashMap<Long, Integer> longMap = new HashMap<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select " + SIGNED_LIST_ID + " from " + TABLE_NAME_2 + " where " + SIGNED_USER_ID + " = '" + userId + "'", null);
            while (cursor.moveToNext()) {
                longMap.put(cursor.getLong(cursor.getColumnIndex(SIGNED_LIST_ID)), 1);
            }
            cursor.close();
        }
        db.close();
        return longMap;
    }

    /**
     * 获取已打卡歌曲
     */
    public HashMap<Long, Integer> getSong(long listId, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        HashMap<Long, Integer> longMap = new HashMap<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select " + SIGNED_SONG_ID + " from " + TABLE_NAME_1 + " where " + SIGNED_LIST_ID + " = " + listId + " and " + SIGNED_USER_ID + " = '" + userId + "'", null);
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
    public void deleteSong(long listId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(TABLE_NAME_1, SIGNED_LIST_ID + " = ? and " + SIGNED_USER_ID + " = ?", new String[]{listId + "", userId});
        }
        db.close();
    }
}
