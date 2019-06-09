package com.mcc.radio.data.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();
    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "radio-app";

    // Table Names
    public static final String TABLE_FAVORITE_PROGRAM = "table_favorite_program";


    // "FAVORITE_PROGRAM" table column names
    public static final String KEY_PROGRAM_ID               = "program_id";
    public static final String KEY_PROGRAM_NAME             = "program_name";
    public static final String KEY_PROGRAM_HOST_NAME        = "program_host_name";
    public static final String KEY_PROGRAM_START_TIME       = "program_start_time";
    public static final String KEY_PROGRAM_END_TIME         = "program_end_time";
    public static final String KEY_PROGRAM_DURATION         = "program_duration";


    // "PROGRAM" table create statement
    private static final String CREATE_TABLE_PROGRAM = "CREATE TABLE "+ TABLE_FAVORITE_PROGRAM + "("
            + KEY_PROGRAM_ID + " INTEGER PRIMARY KEY,"
            + KEY_PROGRAM_NAME + " TEXT,"
            + KEY_PROGRAM_HOST_NAME + " TEXT,"
            + KEY_PROGRAM_START_TIME + " TEXT,"
            + KEY_PROGRAM_END_TIME + " TEXT,"
            + KEY_PROGRAM_DURATION + " TEXT)";


    private static DatabaseHelper dbHelper = null;

    public static DatabaseHelper getInstance(Context context) {
        if(dbHelper == null) {
            dbHelper =  new DatabaseHelper(context);
        }
        return dbHelper;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_PROGRAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_PROGRAM);
        // create new tables
        onCreate(db);
    }

}
