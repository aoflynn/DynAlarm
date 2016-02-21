package me.adamoflynn.dynalarm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Adam O'Flynn on 20/02/2016.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    public static final String DATABASE_NAME = "DynAlarmDatabase.db";

    public static final String USER_TABLE = "user";
    public static final String LOCATION_TABLE = "location";

    private static final String KEY_ID = "id";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LOCLON = "locLon";
    private static final String KEY_LOCLAT = "locLat";


    private static final String CREATE_TABLE_USER = "CREATE TABLE "
            + USER_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ")";

    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE "
            + LOCATION_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY," +
            KEY_LOCATION + " TEXT," + KEY_LOCLON + " REAL," + KEY_LOCLAT + " REAL" + ")";

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
        onCreate(db);
    }
}
