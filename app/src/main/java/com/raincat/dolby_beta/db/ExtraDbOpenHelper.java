package com.raincat.dolby_beta.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/03/30
 *     desc   : 额外信息
 *     version: 1.0
 * </pre>
 */
public class ExtraDbOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static ExtraDbOpenHelper instance;

    private static final String EXTRA_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + ExtraDao.TABLE_NAME + " ("
            + ExtraDao.EXTRA_KEY + " VARCHAR(20) PRIMARY KEY, "
            + ExtraDao.EXTRA_VALUE + " TEXT ); ";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EXTRA_TABLE_CREATE);
    }

    private ExtraDbOpenHelper(Context context) {
        super(context, getUserDatabaseName(), null, DATABASE_VERSION);
    }

    static ExtraDbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ExtraDbOpenHelper(context);
        }
        return instance;
    }

    private static String getUserDatabaseName() {
        return "Extra_" + DATABASE_VERSION + ".db";
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
