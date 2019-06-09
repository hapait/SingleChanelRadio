package com.mcc.radio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mcc.radio.R;
import com.mcc.radio.adapter.ProgramListAdapter;
import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.listeners.ListItemClickListener;
import com.mcc.radio.listeners.MediaRecorderStatusListener;
import com.mcc.radio.listeners.PermissionListener;
import com.mcc.radio.listeners.PlayerStatusListeners;
import com.mcc.radio.model.Program;
import com.mcc.radio.model.ProgramTime;
import com.mcc.radio.model.Programs;
import com.mcc.radio.network.ApiInterface;
import com.mcc.radio.network.HttpParams;
import com.mcc.radio.network.RetrofitClient;
import com.mcc.radio.service.PlayerService;
import com.mcc.radio.utils.ActivityUtils;
import com.mcc.radio.utils.AdUtils;
import com.mcc.radio.utils.AppUtility;
import com.mcc.radio.utils.MyAnimation;
import com.mcc.radio.utils.MyDividerItemDecoration;
import com.mcc.radio.utils.NetworkUtils;
import com.mcc.radio.utils.ProgramNameDisplay;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mcc.radio.utils.NetworkUtils.getConnectivityStatusInt;

public class MainActivity extends BaseActivity implements View.OnClickListener, PlayerStatusListeners, PermissionListener, MediaRecorderStatusListener {

    private Context mContext;
    private int volumeLevel;
    private boolean mBound = false, isPlayerPlaying = false;
    private PlayerService mService;
    private AudioManager audioManager;
    private Intent playerServiceIntent;
    private RecyclerView recyclerViewProgramList;
    private ArrayList<Program> programList = new ArrayList<>();
    private View playerBodyTransparentView;
    private ProgramListAdapter programListAdapter;
    private TextView textViewCurrentProgram, textViewProgramHostName;
    private RelativeLayout relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk;
    private LinearLayout linearLayoutPlayerHolder;
    private BroadcastReceiver myBroadCastReceiver, mInternetConnectivityChangeReceiver;
    private ProgressBar progressBar;
    private ImageView imageViewPower, imageViewShare, imageViewProgress, imageViewPlayPause, imageViewPlayer, imageViewRecord, imageViewProgramListController, imageViewCollapseExpandArrow, imageViewVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
        initView();
        initListeners();

    }

    private void initVariable() {
        mContext = getApplicationContext();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        initializeBroadCastReceiver();
        netConnectionAvailabilityBroadCastReceiver();
        if (this.isPlayerServiceRunning()) {
            /**When activity is destroyed then there will be no reference of the running service.
             *  If the service is already running, then to get the reference of that service 'bindService' method will be called.
             */
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        registerReceivers(mInternetConnectivityChangeReceiver, myBroadCastReceiver);
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        imageViewPlayer = findViewById(R.id.img_player);
        imageViewRecord = findViewById(R.id.img_record);
        imageViewVolume = findViewById(R.id.img_volume);
        imageViewPlayPause = findViewById(R.id.img_play_pause);
        imageViewPower = findViewById(R.id.iv_power);
        imageViewShare = findViewById(R.id.iv_share);
        textViewCurrentProgram = findViewById(R.id.tv_current_program);
        textViewProgramHostName = findViewById(R.id.tv_program_host_name);
        recyclerViewProgramList = findViewById(R.id.rv_program_list);
        linearLayoutPlayerHolder = findViewById(R.id.ll_player_holder);
        imageViewCollapseExpandArrow = findViewById(R.id.img_collapse_expand_arrow);
        imageViewProgramListController = findViewById(R.id.img_program_list_controller);
        playerBodyTransparentView = findViewById(R.id.player_body_transparent_view);
        relativeLayoutPlayerDiskHolder = findViewById(R.id.rl_player_disk_holder);
        relativeLayoutPlayerDisk = findViewById(R.id.rl_player_disk);
        imageViewProgress = findViewById(R.id.iv_progress);
        progressBar = findViewById(R.id.progress_bar);
        getProgramListDataFromApiRequest();

        if (volumeLevel == 0) {
            imageViewVolume.setImageResource(R.drawable.img_volume_off_icon);
        } else {
            imageViewVolume.setImageResource(R.drawable.img_volume_up_icon);
        }
    }

    public void getProgramListDataFromApiRequest() {
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getClient().getProgramList(HttpParams.SHEET_ID, HttpParams.SHEET_NAME).enqueue(new Callback<Programs>() {
            @Override
            public void onResponse(Call<Programs> call, Response<Programs> response) {
                ArrayList<Program> myData = new ArrayList<>();
                myData.addAll(response.body().getPrograms());
                programList.clear();
                programList.addAll(myData);
                showCurrentProgramNameBasedOnTime();
                setRecyclerView();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Programs> call, Throwable t) {

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showCurrentProgramNameBasedOnTime() {
        ProgramNameDisplay programNameDisplay = new ProgramNameDisplay();
        programNameDisplay.setListener(new ProgramNameDisplay.ProgramNameDisplayListener() {
            @Override
            public void onProgramNameFound(String programName, String hostName) {
                textViewCurrentProgram.setText(programName);
                textViewProgramHostName.setText(hostName);
            }
        });
        programNameDisplay.execute(programList);
    }

    private void initListeners() {
        imageViewRecord.setOnClickListener(this);
        imageViewVolume.setOnClickListener(this);
        imageViewPlayPause.setOnClickListener(this);
        imageViewProgramListController.setOnClickListener(this);
        imageViewPower.setOnClickListener(this);
        imageViewShare.setOnClickListener(this);
        ActivityUtils.showAllPrograms(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdUtils.getInstance(this).loadFullScreenAd(this);
    }

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            restorePlayerControllerView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //Log.d("ServiceTesting", "MainActivity -> onServiceDisconnected");
            mBound = false;
            mService = null;
        }

    };

    private void restorePlayerControllerView() {
        if (mService == null) {
            Log.d("ServiceTesting", "MainActivity -> Service is Null");
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        } else {
            Log.d("ServiceTesting", "MainActivity -> Service is not Null");
            if (mService.isPlayerPlaying()) {
                Log.d("ServiceTesting", "MainActivity -> Player is playing");
                imageViewPlayPause.setImageResource(R.drawable.img_pause_icon);
                isPlayerPlaying = true;
                if (!isProgramListControllerPressed) {
                    MyAnimation.rotationAnimator(imageViewPlayer);
                }
            } else {
                Log.d("ServiceTesting", "MainActivity -> Player is not playing");
                imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
            }
            imageViewPlayPause.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        stopRecordingAudio(); // Recording will be stopped when app will be closed;
        isPlayerPlaying = false;
        unRegisterReceivers(mInternetConnectivityChangeReceiver, myBroadCastReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_play_pause:
                if (mService != null && mService.isPlayerPlaying()) {
                    pauseRadioPlayer();
                } else {
                    startRadioPlayer();
                }
                break;

            case R.id.img_program_list_controller:
                showOrHideProgramList(isPlayerPlaying, linearLayoutPlayerHolder, imageViewCollapseExpandArrow, recyclerViewProgramList, playerBodyTransparentView, relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk, imageViewPlayer);
                break;

            case R.id.img_record:
                AppUtility.askAudioRecordPermission(MainActivity.this);
                break;

            case R.id.img_volume:
                showVolumeBar(imageViewVolume);
                break;

            case R.id.iv_power:
                stopRadioPlayer();
                break;

            case R.id.iv_share:
                ActivityUtils.shareAppLink(this);
                break;

        }
    }


    public void setRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        programListAdapter = new ProgramListAdapter(getApplicationContext(), programList, new ListItemClickListener() {
            @Override
            public void onAlarmIconClick(ImageView view, int position) {
                if (!ActivityUtils.isProgramAlarmAlreadySet(MainActivity.this, String.valueOf(programList.get(position).getProgramId()))) {
                    view.setImageResource(R.drawable.img_arlarm_active_icon);
                    ActivityUtils.setProgramAsFavorite(MainActivity.this, programList.get(position));

                    /** Set Alarm*/
                    ProgramTime programTime = AppUtility.getTime(programList.get(position).getProgramStartTime());
                    int alarmRequestCode = programList.get(position).getProgramId();
                    long alarmTimeStamp = AppUtility.getTimeInMillis(programTime.getAlarmTime());
                    if (alarmTimeStamp > AppConstants.VALUE_ZERO) {
                        AppUtility.setAlarm(getApplicationContext(), alarmTimeStamp, alarmRequestCode);
                    }

                    // Show full screen ad // to disable ad remove this bellow line
                    AdUtils.getInstance(MainActivity.this).showFullScreenAd();

                } else {
                    view.setImageResource(R.drawable.img_alarm_inactive_icon);
                    ActivityUtils.removeProgramFromFavorite(MainActivity.this, String.valueOf(programList.get(position).getProgramId()));
                    /** Cancel Alarm*/
                    int alarmRequestCode = programList.get(position).getProgramId();
                    AppUtility.cancelAlarm(getApplicationContext(), alarmRequestCode);
                }
            }
        });
        recyclerViewProgramList.setLayoutManager(mLayoutManager);
        recyclerViewProgramList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewProgramList.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerViewProgramList.setAdapter(programListAdapter);

    }


    /**
     * Start Player Controller Method
     */
    protected void startRadioPlayer() {
        if (getConnectivityStatusInt(this) == AppConstants.VALUE_ZERO) {
            showSnackBar();
        } else {
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setEnabled(false);
            if (mService == null && !mBound) {
                // when service is not created;
                playerServiceIntent = new Intent(MainActivity.this, PlayerService.class);
                startService(playerServiceIntent);
                bindService(playerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            } else if (mService != null && mBound) {
                // when service is already running;
                mService.startPlayer();
            }
        }
    }

    protected void pauseRadioPlayer() {
        if (mService != null) {
            mService.pausePlayer();
        }
    }

    protected void resumeRadioPlayer() {
        if (mService != null) {
            MyAnimation.showLoading(imageViewProgress);
            imageViewPlayPause.setEnabled(false);
            mService.resumePlayerWhenNetConnectionAvailable();
        }
    }

    protected void stopRadioPlayer() {
        if (mService != null) {
            mService.stopPlayer();
            finish();
        } else {
            finish();
        }
    }

    @Override
    public void onMyPlayerStartPlaying() {
        MyAnimation.stopLoading(imageViewProgress);
        isPlayerPlaying = true;
        imageViewPlayPause.setImageResource(R.drawable.img_pause_icon);
        imageViewPlayPause.setEnabled(true);
        if (!isProgramListControllerPressed) {
            MyAnimation.rotationAnimator(imageViewPlayer);
        }
    }

    @Override
    public void onMyPlayerPause() {
        stopRecordingAudio(); // Recording will be stopped when Player will be paused;
        imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        imageViewPlayPause.setEnabled(true);
        isPlayerPlaying = false;
        MyAnimation.stopRotationAnimator();

    }


    @Override
    public void onMyPlayerStop() {
        stopRecordingAudio(); // Recording will be stopped when Player will be stopped;
        imageViewPlayPause.setImageResource(R.drawable.img_play_icon);
        imageViewPlayPause.setEnabled(true);
        MyAnimation.stopRotationAnimator();
        MyAnimation.stopLoading(imageViewProgress);
        unbindService(mConnection);
        Intent playerServiceIntent = new Intent(MainActivity.this, PlayerService.class);
        stopService(playerServiceIntent);
        isPlayerPlaying = false;
        mService = null;
        mBound = false;
        Toast.makeText(this, "Radio stopped playing!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMyPlayerError() {
        Log.e("ExoPlayerTesting", "MainActivity -> onMyPlayerError");
    }


    @Override
    public void onPermissionGranted() {
        startRecordingAudio();
    }
    /** End Player Controller Method */


    /**
     * Start Recording Methods
     */
    private void startRecordingAudio() {
        if (mService != null) {
            //Log.d("MediaRecorder", "startRecordingAudio");
            if (mService.isPlayerPlaying()) {
                if (!mService.isRecorderOn()) {
                    String filePath = AppUtility.createImageFile(this);
                    if (filePath != null) {
                        mService.startRecording(filePath);
                    } else {
                        Toast.makeText(mService, "No External Storage Found!", Toast.LENGTH_SHORT).show();
                    }
                } else
                    mService.stopRecording();
            } else {
                Toast.makeText(this, "Start radio player first!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopRecordingAudio() {
        if (mService != null) {
            if (mService.isRecorderOn())
                mService.stopRecording();
        }
    }

    @Override
    public void onMyRecorderStart() {
        Toast.makeText(this, "Recording Started!", Toast.LENGTH_SHORT).show();
        imageViewRecord.setImageResource(R.drawable.img_record_inactive_icon);
    }

    @Override
    public void onMyRecorderStop() {
        Toast.makeText(this, "File saved in " + getString(R.string.recording_file_storage_directory), Toast.LENGTH_LONG).show();
        imageViewRecord.setImageResource(R.drawable.img_record_active_icon);
    }

    @Override
    public void onMyRecorderError() {
        Toast.makeText(this, "Recording Error!", Toast.LENGTH_SHORT).show();
        imageViewRecord.setImageResource(R.drawable.img_record_active_icon);
    }

    /**
     * End Recording Methods
     */


    private void showVolumeBar(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.volume_controller_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, 100);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int location[] = new int[2];
        anchorView.getLocationOnScreen(location); // Get the View's(the one that was clicked in the Activity) location
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] - 150);// Using location, the PopupWindow will be displayed right under anchorView

        SeekBar seekBarVolumeControl = popupView.findViewById(R.id.seek_bar_volume);
        seekBarVolumeControl.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolumeControl.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBarVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                if (i == 0) {
                    imageViewVolume.setImageResource(R.drawable.img_volume_off_icon);
                } else {
                    imageViewVolume.setImageResource(R.drawable.img_volume_up_icon);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * Start Broadcast Receiver Methods
     */
    private void initializeBroadCastReceiver() {
        myBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d("ExoPlayerTesting", "MainActivity -> Broadcast Message  Received!");
                if (mBound) {
                    String status = intent.getStringExtra("STATUS");
                    switch (status) {
                        case AppConstants.ON_START_PLAYING:
                            MainActivity.this.onMyPlayerStartPlaying();
                            break;
                        case AppConstants.ON_PLAYER_PAUSE:
                            MainActivity.this.onMyPlayerPause();
                            break;
                        case AppConstants.ON_PLAYER_STOP:
                            MainActivity.this.onMyPlayerStop();
                            break;
                        case AppConstants.ON_PLAYER_ERROR:
                            //Log.d("ExoPlayerTesting", "MainActivity -> onPlayerError");
                            MainActivity.this.onMyPlayerError();
                            break;
                        case AppConstants.ON_START_RECORDING:
                            //Log.d("MediaRecorder", "MainActivity -> onStartRecording");
                            MainActivity.this.onMyRecorderStart();
                            break;
                        case AppConstants.ON_STOP_RECORDING:
                            //Log.d("MediaRecorder", "MainActivity -> onStopRecording");
                            MainActivity.this.onMyRecorderStop();
                            break;
                        case AppConstants.ON_RECORDING_ERROR:
                            //Log.d("MediaRecorder", "MainActivity -> onRecordingError");
                            MainActivity.this.onMyRecorderError();
                            break;
                    }
                }

            }
        };
    }

    protected void netConnectionAvailabilityBroadCastReceiver() {
        mInternetConnectivityChangeReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = NetworkUtils.getConnectivityStatusInt(context);
                if (status == AppConstants.VALUE_ZERO) {
                    //Log.d("NetConnectivityStatus", "Not Available");
                    MainActivity.this.pauseRadioPlayer();
                    showSnackBar();
                } else {

                    if (isInitialStickyBroadcast()) {
                        // Do Nothing;
                    } else {
                        //Log.d("NetConnectivityStatus", "Available");
                        MainActivity.this.resumeRadioPlayer();
                        hideSnackBar();
                    }

                }
            }
        };
    }
    /** End Broadcast Receiver Methods*/

}
