package com.raincat.dolby_beta.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <pre>
 *     author : RainCat
 *     e-mail : nining377@gmail.com
 *     time   : 2020/03/24
 *     desc   : 云盘缓存
 *     version: 1.0
 * </pre>
 */
public class CloudDbOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static CloudDbOpenHelper instance;

    private static final String CLOUD_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + CloudDao.TABLE_NAME + " ("
            + CloudDao.SONG_ID + " INTEGER PRIMARY KEY, "
            + CloudDao.SONG_VALUE + " TEXT)";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CLOUD_TABLE_CREATE);
    }

    private CloudDbOpenHelper(Context context) {
        super(context, getUserDatabaseName(), null, DATABASE_VERSION);
    }

    static CloudDbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CloudDbOpenHelper(context);
        }
        return instance;
    }

    private static String getUserDatabaseName() {
        return "Cloud_" + DATABASE_VERSION + ".db";
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
