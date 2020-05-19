package com.raincat.dolby_beta.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/16
 *     desc   : 歌曲打卡
 *     version: 1.0
 * </pre>
 */
public class SignDbOpenHelper  extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static SignDbOpenHelper instance;

    private static final String SONG_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SignDao.TABLE_NAME_1 + " ("
            + SignDao.SIGNED_SONG_ID + " BIGINT PRIMARY KEY, "
            + SignDao.SIGNED_LIST_ID + " BIGINT , "
            + SignDao.SIGNED_USER_ID + " VARCHAR(20) ); ";

    private static final String LIST_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SignDao.TABLE_NAME_2 + " ("
            + SignDao.SIGNED_USER_ID + " VARCHAR(20) , "
            + SignDao.SIGNED_LIST_ID + " BIGINT PRIMARY KEY ); ";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SONG_TABLE_CREATE);
        db.execSQL(LIST_TABLE_CREATE);
    }

    private SignDbOpenHelper(Context context) {
        super(context, getUserDatabaseName(), null, DATABASE_VERSION);
    }

    static SignDbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SignDbOpenHelper(context);
        }
        return instance;
    }

    private static String getUserDatabaseName() {
        return "SignedSong_" + DATABASE_VERSION + ".db";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void close() {
        if (instance != null) {
            try {
                SQLiteDatabase db = instance.getWritableDatabase();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
}
