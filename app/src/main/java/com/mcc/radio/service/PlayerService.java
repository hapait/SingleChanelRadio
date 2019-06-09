package com.mcc.radio.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.mcc.radio.R;
import com.mcc.radio.activity.MainActivity;
import com.mcc.radio.app.MyApplication;
import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.listeners.MediaRecorderListener;
import com.mcc.radio.listeners.PlayerListener;
import com.mcc.radio.player.PlayerManager;
import com.mcc.radio.utils.AppUtility;


public class PlayerService extends Service {

    Notification notification;
    RemoteViews mNotificationView;
    NotificationCompat.Builder mBuilder;
    public PlayerManager mPlayerManager;
    NotificationManager mNotificationManager;
    private static PlayerService playerService;
    private final IBinder mBinder = new LocalBinder();
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;

    /** Start Player Service Methods */
    public PlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("PlayerService", "onStartCommand");
        startPlayer();
        pushServiceToForeground();
        initializePhoneStateListener();
        playerService = this;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d("PlayerService", "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("PlayerService", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    public void pushServiceToForeground() {
        startForeground(AppConstants.FOREGROUND_NOTIFICATION_REQUEST_CODE, generateNotification(getApplicationContext()));
    }
    /** End Player Service Methods */



    /** Start Service Notification*/
    private Notification generateNotification(Context mContext) {
        String actionStop = getApplicationContext().getPackageName()+AppConstants.ACTION_STOP;
        Intent stopIntent = new Intent(actionStop);
        PendingIntent stopRadioPendingIntent = PendingIntent.getBroadcast(this, AppConstants.STOP_RADIO_INTENT_REQUEST_CODE, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(AppConstants.CONTENT_INTENT_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationView = new RemoteViews(getPackageName(), R.layout.foreground_notification_layout);
        mNotificationView.setOnClickPendingIntent(R.id.img_stop_player, stopRadioPendingIntent);

        mBuilder = new NotificationCompat.Builder(mContext, AppConstants.CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.img_radio_icon);
        mBuilder.setContent(mNotificationView);
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(resultPendingIntent);
        notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        return notification;
    }


    private void updateNotificationWhenPlayerPlaying() {
        // when player is playing then set pause icon in remote view and update the intent action;
        String actionPause = getApplicationContext().getPackageName()+AppConstants.ACTION_PAUSE;
        Intent pauseIntent = new Intent(actionPause);
        PendingIntent pauseRadioPendingIntent = PendingIntent.getBroadcast(this, AppConstants.PAUSE_INTENT_REQUEST_CODE, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationView.setImageViewResource(R.id.img_notification_play_pause, R.drawable.img_pause_icon_notify);
        mNotificationView.setOnClickPendingIntent(R.id.img_notification_play_pause, pauseRadioPendingIntent);
        mNotificationManager.notify(AppConstants.FOREGROUND_NOTIFICATION_REQUEST_CODE, mBuilder.build());
    }

    private void updateNotificationWhenPlayerPaused() {
        // when player is paused then set play icon in remote view and update the intent action;
        String actionPlay = getApplicationContext().getPackageName()+AppConstants.ACTION_PLAY;
        Intent playIntent = new Intent(actionPlay);
        PendingIntent playRadioPendingIntent = PendingIntent.getBroadcast(this, AppConstants.PLAY_INTENT_REQUEST_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationView.setImageViewResource(R.id.img_notification_play_pause, R.drawable.img_play_icon_notify);
        mNotificationView.setOnClickPendingIntent(R.id.img_notification_play_pause, playRadioPendingIntent);
        mNotificationManager.notify(AppConstants.FOREGROUND_NOTIFICATION_REQUEST_CODE, mBuilder.build());
    }
    /** End Service Notification */


    /** Start Player Controlling Methods */
    public void startPlayer() {
        mPlayerManager = PlayerManager.getInstance();
        mPlayerManager.startExoPlayer(getApplicationContext(), new PlayerListener() {

            @Override
            public void onStartPlaying() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_START_PLAYING);
                updateNotificationWhenPlayerPlaying();
            }

            @Override
            public void onPlayerPause() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_PAUSE);
                updateNotificationWhenPlayerPaused();
            }

            @Override
            public void onPlayerStop() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_STOP);
            }

            @Override
            public void onPlayerError() {
                //Log.d("ExoPlayerTesting", "PlayerService -> onPlayerError");
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_ERROR);
            }
        });
    }

    public void pausePlayer() {
        mPlayerManager.pauseExoPlayer();
    }

    public void resumePlayerWhenNetConnectionAvailable() {
        mPlayerManager.resumeExoPlayerWhenNetConnectionAvailable(getApplicationContext());
    }

    public void stopPlayer() {
        mPlayerManager.stopExoPlayer();
    }

    public boolean isPlayerPlaying() {
        if (mPlayerManager.isExoPlayerPlaying())
            return true;
        else
            return false;
    }
    /** End Player Controlling Methods */


    /** Start Recorder Controlling Methods */
    public void startRecording(String filePath) {
        mPlayerManager.startMediaRecorder(filePath, new MediaRecorderListener() {
            @Override
            public void onRecordingStart() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_START_RECORDING);
            }

            @Override
            public void onRecordingStop() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_STOP_RECORDING);
            }

            @Override
            public void onRecordingError() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_RECORDING_ERROR);
            }
        });
    }

    public void stopRecording() {
        mPlayerManager.stopMediaRecorder();
    }

    public boolean isRecorderOn() {
        return mPlayerManager.isMediaRecorderOn();
    }
    /** End Recorder Controlling Methods */



    /** Start Listeners And BroadCastReceivers Method*/
    private void initializePhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (lastState == state) {
                //No change, de_bounce extras;
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!isPlayerPlaying()) {
                        startPlayer();
                    }
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (isPlayerPlaying()) {
                        pausePlayer();
                    }
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    if (isPlayerPlaying()) {
                        pausePlayer();
                    }
                    break;
            }
            lastState = state;
        }

    }


    public static class AudioPlayerBroadcastReceiver extends BroadcastReceiver {

        public AudioPlayerBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();

            if(actionName.equals(getAction(AppConstants.ACTION_PLAY))){
                //Log.d("NotificationTesting", "PlayerService -> AudioPlayerBroadcastReceiver -> ACTION_PLAY");
                playerService.startPlayer();
            }
            else if(actionName.equals(getAction(AppConstants.ACTION_PAUSE))){
                //Log.d("NotificationTesting", "PlayerService -> AudioPlayerBroadcastReceiver -> ACTION_PAUSE");
                playerService.pausePlayer();
            }
            else if(actionName.equals(getAction(AppConstants.ACTION_STOP))) {
                //Log.d("NotificationTesting", "PlayerService -> AudioPlayerBroadcastReceiver -> ACTION_STOP");
                playerService.stopPlayer();
                playerService.stopSelf();
            }

        }
    }

    public static String getAction(String actionName){
        return MyApplication.getAppContext().getPackageName()+actionName;
    }

    /** End Listeners And BroadCastReceivers Method*/
}
