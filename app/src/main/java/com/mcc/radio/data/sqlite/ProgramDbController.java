package com.mcc.radio.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.model.Program;

import java.util.ArrayList;

public class ProgramDbController {

    private DatabaseHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase database;

    public ProgramDbController(Context context) {
        mContext = context;
    }

    public ProgramDbController open() throws SQLException {
        dbHelper = new DatabaseHelper(mContext);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertFavouriteItem(int programId, String programName, String programHostName, String programStartTime, String programEndTime, String programDuration) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.KEY_PROGRAM_ID, programId);
        contentValue.put(DatabaseHelper.KEY_PROGRAM_NAME, programName);
        contentValue.put(DatabaseHelper.KEY_PROGRAM_HOST_NAME, programHostName);
        contentValue.put(DatabaseHelper.KEY_PROGRAM_START_TIME, programStartTime);
        contentValue.put(DatabaseHelper.KEY_PROGRAM_END_TIME, programEndTime);
        contentValue.put(DatabaseHelper.KEY_PROGRAM_DURATION, programDuration);

        return database.insert(DatabaseHelper.TABLE_FAVORITE_PROGRAM, null, contentValue);
    }

    public ArrayList<Program> getAllProgramData() {
        ArrayList<Program> programList = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + DatabaseHelper.TABLE_FAVORITE_PROGRAM, null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    int programId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_ID));
                    String programName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_NAME));
                    String programHostName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_HOST_NAME));
                    String programStartTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_START_TIME));
                    String programEndTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_END_TIME));
                    String programDuration = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PROGRAM_DURATION));
                    if (programId > AppConstants.VALUE_ZERO) {
                        programList.add(new Program(programId, programName, programHostName, programStartTime, programEndTime, programDuration));
                    }
                    cursor.moveToNext();
                }
            } catch (Exception ex) {
            }
        }
        return programList;
    }

    public int updateFavouriteItem(int programId, String programName, String programHostName, String programStartTime, String programEndTime, String programDuration, String programAlarmStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_PROGRAM_ID, programId);
        contentValues.put(DatabaseHelper.KEY_PROGRAM_NAME, programName);
        contentValues.put(DatabaseHelper.KEY_PROGRAM_HOST_NAME, programHostName);
        contentValues.put(DatabaseHelper.KEY_PROGRAM_START_TIME, programStartTime);
        contentValues.put(DatabaseHelper.KEY_PROGRAM_END_TIME, programEndTime);
        contentValues.put(DatabaseHelper.KEY_PROGRAM_DURATION, programDuration);

        int updateStatus = database.update(DatabaseHelper.TABLE_FAVORITE_PROGRAM, contentValues,
                DatabaseHelper.KEY_PROGRAM_ID + " = " + programId, null);
        return updateStatus;
    }

    public void deleteFavouriteItemById(int productId) {
        database.delete(DatabaseHelper.TABLE_FAVORITE_PROGRAM, DatabaseHelper.KEY_PROGRAM_ID + "=" + productId, null);
    }

    public boolean isAlreadyFavourite(String productId) {
        Cursor cursor = database.rawQuery("select " + DatabaseHelper.KEY_PROGRAM_ID + " from " + DatabaseHelper.TABLE_FAVORITE_PROGRAM + " where " + DatabaseHelper.KEY_PROGRAM_ID + "=" + productId + "", null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void deleteAllFavouriteData() {
        database.delete(DatabaseHelper.TABLE_FAVORITE_PROGRAM, null, null);
    }

    public int countFavouriteProduct() {
        int numOfRows = (int) DatabaseUtils.queryNumEntries(database, DatabaseHelper.TABLE_FAVORITE_PROGRAM);
        return numOfRows;
    }

    private void dropFavouriteTable() {
        String sql = "drop table " + DatabaseHelper.TABLE_FAVORITE_PROGRAM;
        try {
            database.execSQL(sql);
        } catch (SQLException e) {

        }
    }

}
