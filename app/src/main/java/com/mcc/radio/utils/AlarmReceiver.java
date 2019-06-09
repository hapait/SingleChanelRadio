package com.mcc.radio.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.mcc.radio.R;
import com.mcc.radio.activity.MainActivity;
import com.mcc.radio.data.constant.AppConstants;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {
        String alarmIntent = intent.getStringExtra(AppConstants.ALARM_INTENT_NAME);
        //Log.d("AlarmTesting", alarmIntent);
        switch (alarmIntent) {
            case AppConstants.RADIO_PROGRAM_NOTIFICATION_ALARM:
                sendNotification(mContext);
                break;
        }
    }

    private void sendNotification(Context mContext) {
        Intent intent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(AppConstants.PROGRAM_NOTIFICATION_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews notificationLayout = new RemoteViews(mContext.getPackageName(), R.layout.program_notification_layout);
        Notification customNotification = new NotificationCompat.Builder(mContext, AppConstants.CHANNEL_ID)
                .setSmallIcon(R.drawable.img_radio_icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(resultPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(AppConstants.PROGRAM_NOTIFICATION_NOTIFICATION_ID, customNotification);
    }
}
