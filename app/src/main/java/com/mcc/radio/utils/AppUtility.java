package com.mcc.radio.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mcc.radio.R;
import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.listeners.PermissionListener;
import com.mcc.radio.model.ProgramTime;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AppUtility {

    private static AppUtility mAppUtility = null;

    // create single instance
    public static AppUtility getInstance() {
        if (mAppUtility == null) {
            mAppUtility = new AppUtility();
        }
        return mAppUtility;
    }

    public static void askAudioRecordPermission(Activity activity) {
        if (PermissionUtils.isPermissionGranted(activity, PermissionUtils.PERMISSIONS_REQUIRED, PermissionUtils.PERMISSION_REQUEST_CALLBACK_CONSTANT)) {
            PermissionListener permissionListener = (PermissionListener) activity;
            //Log.d("MediaRecorder", "Found 1");
            permissionListener.onPermissionGranted();
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String createImageFile(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = mContext.getString(R.string.app_name) + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), mContext.getString(R.string.recording_file_storage_directory));
        if (!storageDir.exists()) {
            storageDir.mkdirs();
            Log.d("PathTesting", storageDir.getAbsolutePath());
        }
        File audioFile = null;
        try {
            audioFile = File.createTempFile(
                    audioFileName,  /* prefix */
                    ".3gp",  /* suffix */
                    storageDir     /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        String absolutePath = audioFile.getAbsolutePath();
        if (absolutePath != null)
            return absolutePath;
        else
            return null;
    }

    public static void sendBroadCastMessages(Context mContext, String statusMessages) {
        Intent broadCastIntent = new Intent(AppConstants.MY_BROADCAST_RECEIVER);
        broadCastIntent.putExtra("STATUS", statusMessages);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadCastIntent);
    }

    public static void setAlarm(Context context, long timeStamp, int alarmRequestCode) {
        //Log.d("AlarmTesting", "AlarmTime " + timeStamp);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(AppConstants.ALARM_INTENT_NAME, AppConstants.RADIO_PROGRAM_NOTIFICATION_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, alarmIntent, 0);

        if (timeStamp > System.currentTimeMillis()) {
            Log.d("AlarmTesting ", "AlarmTime  " + timeStamp + "  SystemTime" + System.currentTimeMillis());
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeStamp, AlarmManager.INTERVAL_DAY, pendingIntent);
            } else {
                manager.setRepeating(AlarmManager.RTC_WAKEUP, timeStamp, AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
    }

    public static void cancelAlarm(Context mContext, int alarmRequestCode) {
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, alarmRequestCode, alarmIntent, 0);
        manager.cancel(pendingIntent);
        //Log.d("AlarmTesting", "RadioProgramAlarm is Cancel");
    }

    public static long getTimeInMillis(String time) {
        //Log.d("AlarmTesting", "getTimeInMillis " + time);
        SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a");
        Date date;
        try {
            date = datetimeFormatter1.parse(time); //"14.03.2018 5:40:00 pm"
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * this method parse a string and return 'ProgramTime' object;
     **/
    public static ProgramTime getTime(String time) {
        // "12.03.2018 5:40 pm Monday";
        String[] parts = time.split(" ");
        String dateMonthYear = parts[0]; // 12.03.2018
        String hourMinute = parts[1]; // 5:40
        String amPM = parts[2]; // pm
        String day = parts[3]; // Monday
        return new ProgramTime(dateMonthYear + " " + hourMinute + ":00 " + amPM, hourMinute + " " + amPM, day);
    }

    public static int getMargin(Context context) {
        int margin = 0;
        switch (getDeviceDensity(context)) {
            case "xxxhdpi":
                margin = 150;
                break;
            case "xxhdpi":
                margin = 380;
                break;
            case "xhdpi":
                margin = 330;
                break;
            case "hdpi":
                margin = 180;
                break;
            case "mdpi":
                margin = 180;
                break;
        }
        return margin;

    }

    private static String getDeviceDensity(Context context) {
        double density = context.getResources().getDisplayMetrics().density;
        if (density >= 4.0) {
            return "xxxhdpi";
        } else if (density >= 3.0 && density < 4.0) {
            return "xxhdpi";
        } else if (density >= 2.0) {
            return "xhdpi";
        } else if (density >= 1.5 && density < 2.0) {
            return "hdpi";
        } else if (density >= 1.0 && density < 1.5) {
            return "mdpi";
        }
        return null;
    }


}
